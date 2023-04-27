package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.slf4j.Logger;

public class StreamGobblerThread extends Thread {

    public static final int INFO = 1;
    public static final int ERROR = 2;
    InputStream is;
    private Logger log;
    private String prefix;
    private int flag;
    private Color color;
    private String line = null;
    private boolean logEnd = false;
    private boolean charMode = false;

    public boolean isLogEnd() {
        return logEnd;
    }

    public void setCharMode(boolean charMode) {
        this.charMode = charMode;
    }

    public StreamGobblerThread(InputStream is, Logger log, String prefix, int flag,
        Ansi.Color color) {
        this.is = is;
        this.log = log;
        this.prefix = prefix;
        this.flag = flag;
        this.color = color;
    }

    public void waitLog(){
        while(!logEnd){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(is);
            if (!charMode) {
                br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    if (flag == INFO) {
                        log.info(AnsiColorBuilder.build(color, prefix + line));
                    } else if (flag == ERROR) {
                        log.error(AnsiColorBuilder.red(prefix + line));
                    }
                }
            } else {
                char buf[] = new char[2048];
                long st = System.currentTimeMillis();
                while(true) {
                    if(System.currentTimeMillis() - st > 1000L){
                        logEnd = true;
                    }
                    if (isr.ready()) {
                        int n = isr.read(buf, 0, 1024);
                        if (n > 0) {
                            logEnd = false;
                            String x = new String(buf, 0, n);
                            log.info(AnsiColorBuilder.build(color, prefix + x));
                            st = System.currentTimeMillis();
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        } catch (IOException ioe) {
            log.error("ssh exception:{}", ExceptionUtils.getStackTrace(ioe));
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }
}
