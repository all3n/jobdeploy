package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobblerThread extends Thread {
  public static final int INFO = 1;
  public static final int ERROR = 2;
  InputStream is;
  private Logger log;
  private String prefix;
  private int flag;
  private Color color;

  public StreamGobblerThread(InputStream is, Logger log, String prefix, int flag, Ansi.Color color) {
    this.is = is;
    this.log = log;
    this.prefix = prefix;
    this.flag = flag;
    this.color = color;
  }

  public void run() {
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        if (flag == INFO) {
          log.info(AnsiColorBuilder.build(color, prefix + line));
        } else if (flag == ERROR) {
          log.error(AnsiColorBuilder.red(prefix + line));
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
