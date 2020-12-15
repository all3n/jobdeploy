package com.devhc.jobdeploy.ssh;

import ch.ethz.ssh2.*;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
public class SSHDriver extends DeployDriver {

    private String username;
    private String hostname;
    private Connection conn;
    private SFTPv3Client sftpClient;
    private SCPClient scpClient;
    private int timeout = 60;
    private static Logger log = Loggers.get();
    private Ansi.Color color = Ansi.Color.DEFAULT;
    private String keyfilePass;
    private String keyfile;
    private String password;

    public static String AGENT_SOCK_ENV = "SSH_AUTH_SOCK";

    public SSHDriver(String hostname, String username) throws IOException {
        this.username = username;
        this.hostname = hostname;
        conn = new Connection(hostname);
        conn.connect();
    }
    @Override
    public void init() throws IOException {
        boolean isAuthenticated = false;
        if (StringUtils.isNotEmpty(keyfile)) {
            isAuthenticated = conn.authenticateWithPublicKey(username, new File(keyfile), keyfilePass);
            Preconditions.checkArgument(isAuthenticated, "key auth fail" + keyfile);
        } else if (StringUtils.isNotEmpty(password)) {
            isAuthenticated = conn.authenticateWithPassword(username,
                    password);
            Preconditions.checkArgument(isAuthenticated, "password auth fail");
        }
    }


    public boolean exists(String dirName) {
        try {
            getSftpClient().ls(dirName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    @Override
    public void execCommand(String command) {
        if (isSudo()) {
            command = "sudo " + command;
        }
        log.info(AnsiColorBuilder.build(color, "[" + username + "@" + conn.getHostname() + "]:"
                + command));
        Session sess = null;
        try {
            sess = conn.openSession();
            sess.execCommand(command);
            StreamGobblerThread t1 = new StreamGobblerThread(sess.getStdout(),
                    log, "[" + username + "@" + conn.getHostname() + "]:",
                    StreamGobblerThread.INFO, color);
            StreamGobblerThread t2 = new StreamGobblerThread(sess.getStderr(),
                    log, "[" + username + "@" + conn.getHostname() + "]",
                    StreamGobblerThread.ERROR, color);
            t1.start();
            t2.start();

            int ret = sess.waitForCondition(ChannelCondition.EOF
                    | ChannelCondition.EXIT_STATUS
                    | ChannelCondition.STDERR_DATA
                    | ChannelCondition.STDOUT_DATA, timeout * 1000L);

            if ((ret & ChannelCondition.TIMEOUT) != 0) {
                throw new DeployException("[" + conn.getHostname() + "]:"
                        + command + " Timeout");
            }
            t1.join();
            t2.join();
        } catch (IOException e) {
            throw new DeployException(e.getMessage());
        } catch (InterruptedException e) {
            log.error("ssh interrupt:{}", ExceptionUtils.getStackTrace(e));
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
    }

    public SFTPv3Client getSftpClient() {
        if (sftpClient == null) {
            try {
                sftpClient = new SFTPv3Client(conn);
            } catch (IOException e) {
                throw new DeployException(e);
            }
        }
        return sftpClient;
    }

    public SCPClient getScpClient() {
        if (scpClient == null) {
            try {
                scpClient = conn.createSCPClient();
            } catch (IOException e) {
                throw new DeployException(e);
            }
        }
        return scpClient;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (conn != null) {
            if (sftpClient != null) {
                sftpClient.close();
            }
            conn.close();
        }
    }

    @Override
    public void put(String sourceFile, String target) throws IOException {
        this.getScpClient().put(sourceFile, target);
    }

    @Override
    public List<Pair<String, Long>> ls(String dir) throws IOException {
        List<SFTPv3DirectoryEntry> sftpFileList = this.getSftpClient().ls(dir);
        List<Pair<String, Long>> res = Lists.newArrayList();
        sftpFileList.forEach(f -> res.add(Pair.of(f.filename, Long.valueOf(f.attributes.mtime * 1000L))));
        return res;
    }

}
