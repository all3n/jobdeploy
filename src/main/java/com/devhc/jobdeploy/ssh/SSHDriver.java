package com.devhc.jobdeploy.ssh;

import ch.ethz.ssh2.*;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class SSHDriver {
  private String username;
  private String hostname;
  private Connection conn;
  private SFTPv3Client sftpClient;
  private SCPClient scpClient;
  private int timeout = 10;
  private static Logger log = LoggerFactory.getLogger(SSHDriver.class);
  private boolean sudo;
  private Ansi.Color color = Ansi.Color.DEFAULT;

  public SSHDriver(String hostname, String username, String keyfileName,
    String keyfilePass) throws IOException {
    this.username = username;
    File keyfile = new File(keyfileName);
    this.hostname = hostname;
    conn = new Connection(hostname);
    conn.connect();
    boolean isAuthenticated = conn.authenticateWithPublicKey(username,
      keyfile, keyfilePass);
    if (isAuthenticated == false) {
      throw new DeployException("Authentication failed." + "username:"
        + username + " keyfile:" + keyfile + " keyfilePass:"
        + keyfilePass);
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

  public SSHDriver(String hostname, String username, String password)
    throws IOException {
    conn = new Connection(hostname);
    conn.connect();
    boolean isAuthenticated = conn.authenticateWithPassword(username,
      password);
    if (isAuthenticated == false) {
      throw new DeployException("Authentication failed." + "username:"
        + username + " password:" + password);
    }
  }

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
        | ChannelCondition.STDOUT_DATA, timeout * 1000l);

      if ((ret & ChannelCondition.TIMEOUT) != 0) {
        throw new DeployException("[" + conn.getHostname() + "]:"
          + command + " Timeout");
      }
      t1.join();
      t2.join();
    } catch (IOException e) {
      throw new DeployException(e.getMessage());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (sess != null) {
        sess.close();
      }
    }
  }

  public void mkdir(String dir, String chmod, String chown) {
    if (".".equals(dir)) {
      return;
    }
    String initRemoteDir = "mkdir -p " + dir;
    execCommand(initRemoteDir);
    changePermission(dir, chmod, chown);
  }

  public void changePermission(String file, String chmod, String chown,
    boolean recursion) {
    if (StringUtils.isNotEmpty(chmod)) {
      if (recursion) {
        execCommand("chmod -R " + chmod + " " + file);
      } else {
        execCommand("chmod " + chmod + " " + file);
      }
    }
    if (StringUtils.isNotEmpty(chown)) {
      if (recursion) {
        execCommand("chown -R " + chown + " " + file);
      } else {
        execCommand("chown " + chown + " " + file);
      }
    }

  }

  public void changePermission(String file, String chmod, String chown) {
    changePermission(file, chmod, chown, false);
  }

  public SFTPv3Client getSftpClient() {
    if (sftpClient == null) {
      try {
        sftpClient = new SFTPv3Client(conn);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    return sftpClient;
  }

  public SCPClient getScpClient() {
    if (scpClient == null) {
      try {
        scpClient = conn.createSCPClient();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
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

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public boolean isSudo() {
    return sudo;
  }

  public void setSudo(boolean sudo) {
    this.sudo = sudo;
  }

  public Ansi.Color getColor() {
    return color;
  }

  public void setColor(Ansi.Color color) {
    this.color = color;
  }

}
