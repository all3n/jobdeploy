package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author wanghuacheng
 */
@Data
public class JschDriver extends DeployDriver {

  private String username;
  private String hostname;
  private int timeout = 60;
  private static Logger log = Loggers.get();
  private String keyfilePass;
  private String keyfile;
  private String password;
  private JSch jSch;
  private Session sess;
  private String proxyServer;

  private static final Integer MAX_RETRY_RECONNECT_TIMES = 5;

  public JschDriver(String hostname, String username) throws IOException {
    this.username = username;
    this.hostname = hostname;
  }

  enum AUTH_TYPE {
    PASSWORD, KEY_FILE, SSH_AGENT, PAGEANT
  }

  @Override
  public void init() {
    if (jSch != null && sess != null && sess.isConnected()) {
      return;
    }
    this.jSch = new JSch();
    Proxy proxy = null;
    if (StringUtils.isNotEmpty(proxyServer)) {
      String proxyInfo[] = proxyServer.split(":");
      String host = proxyInfo[0];
      Integer port = Integer.parseInt(proxyInfo[1]);
      Sock5ProxyJsch proxy2 = new Sock5ProxyJsch(host, port);
      proxy = proxy2;
      log.debug("{} use sock5 proxy:{}:{}", hostname, host, port);
    }
    String authTypeStr = getDeployJson().getAuthType().toLowerCase();
    AUTH_TYPE authType = AUTH_TYPE.PASSWORD;
    if (StringUtils.isNotEmpty(authTypeStr)) {
      if ("password".equals(authTypeStr)) {
        authType = AUTH_TYPE.PASSWORD;
      } else if ("keyfile".equals(authTypeStr)) {
        authType = AUTH_TYPE.KEY_FILE;
      } else if ("ssh_agent".equals(authTypeStr)) {
        authType = AUTH_TYPE.SSH_AGENT;
      } else if ("pageant".equals(authTypeStr)) {
        authType = AUTH_TYPE.PAGEANT;
      }
    } else {
      if (StringUtils.isNotEmpty(password)) {
        authType = AUTH_TYPE.PASSWORD;
      } else if (StringUtils.isNotEmpty(keyfile)) {
        authType = AUTH_TYPE.KEY_FILE;
      } else if (SSHAgentConnector.isConnectorAvailable()) {
        authType = AUTH_TYPE.SSH_AGENT;
      } else if (PageantConnector.isConnectorAvailable()) {
        authType = AUTH_TYPE.PAGEANT;
      }
    }
    log.debug("{} auth type: {}", hostname, authType);
    switch (authType) {
      case PASSWORD:
        authPassword(proxy);
        break;
      case KEY_FILE:
        authKeyfile(proxy);
        break;
      case SSH_AGENT:
        authSshAgent(proxy);
        break;
      case PAGEANT:
        authPutty(proxy);
        break;
    }
  }

  private void authPutty(Proxy proxy) {
    boolean isAuthenticated;
    // for putty
    try {
      PageantConnector con = new PageantConnector();
      jSch.setIdentityRepository(new RemoteIdentityRepository(con));
      sess = jSch.getSession(username, hostname);
      sess.setProxy(proxy);
      sess.setConfig("StrictHostKeyChecking", "no");
      sess.connect();
      isAuthenticated = true;
    } catch (AgentProxyException | JSchException e) {
      valid = false;
      error = "connect fail!";
      log.error("{}:{}", hostname, e.getMessage());
    }
  }

  private void authSshAgent(Proxy proxy) {
    // ssh -A
    try {
      SSHAgentConnector con = new SSHAgentConnector(new JNAUSocketFactory());
      jSch.setIdentityRepository(new RemoteIdentityRepository(con));
      sess = jSch.getSession(username, hostname);
      sess.setConfig("StrictHostKeyChecking", "no");
      sess.setProxy(proxy);
      sess.connect();
    } catch (AgentProxyException | JSchException e) {
      valid = false;
      error = "connect fail!";
      log.error("{}:{}", hostname, e.getMessage());
    }
  }

  private void authKeyfile(Proxy proxy) {
    try {
      jSch.addIdentity(keyfile, keyfilePass);
      sess = jSch.getSession(username, hostname);
      sess.setConfig("StrictHostKeyChecking", "no");
      sess.setProxy(proxy);
      sess.connect();
    } catch (JSchException e) {
      valid = false;
      error = "connect fail!";
      log.error("{}:{}", hostname, e.getMessage());
    }
  }

  private void authPassword(Proxy proxy) {
    try {
      if (password == null) {
        password = deployJson.getCustom("password");
      }
      Preconditions.checkNotNull(username, "username not null");
      Preconditions.checkNotNull(password, "password not null");
      sess = jSch.getSession(username, hostname);
      sess.setPassword(password);
      sess.setConfig("StrictHostKeyChecking", "no");
      sess.setProxy(proxy);
      sess.connect();
    } catch (JSchException e) {
      valid = false;
      error = "connect fail!";
      log.error("{}:{}", hostname, e.getMessage());
    }
  }

  private boolean reconnectedIfNeeded() {
    if (!sess.isConnected()) {
      log.warn("{} is not connected,try reconnected", hostname);
      int i = 0;
      while (i < MAX_RETRY_RECONNECT_TIMES) {
        try {
          sess.connect();
          break;
        } catch (JSchException e) {
          i += 1;
          try {
            Thread.sleep(100);
          } catch (InterruptedException ex) {
          }
          log.warn("{}:{}", hostname, e.getMessage());
        }
      }
      if (i == MAX_RETRY_RECONNECT_TIMES) {
        valid = false;
      }
    }
    return valid;
  }

  @Override
  public void execCommand(String command) {
    ChannelExec ce = null;
    if(!reconnectedIfNeeded()){
      return;
    }
    try {
      ce = (ChannelExec) sess.openChannel("exec");
      ce.setCommand(command);

      StreamGobblerThread t1 = new StreamGobblerThread(ce.getInputStream(),
          log, "[" + username + "@" + hostname + "]:",
          StreamGobblerThread.INFO, getColor());
      StreamGobblerThread t2 = new StreamGobblerThread(ce.getErrStream(),
          log, "[" + username + "@" + hostname + "]",
          StreamGobblerThread.ERROR, getColor());
      t1.start();
      t2.start();

      ce.connect();

      t1.join();
      t2.join();
    } catch (IOException | JSchException | InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (ce != null) {
        ce.disconnect();
      }
    }

  }

  class SFtpDeployMonitor implements SftpProgressMonitor {

    private ProgressBar pb;

    @Override
    public void init(int op, String src, String dest, long max) {
      String name = new File(src).getName();
      this.pb = new ProgressBarBuilder()
          .setInitialMax(max)
          .showSpeed()
//          .setConsumer(new DelegatingProgressBarConsumer(log::info))
          .setUnit("MiB", 1048576)
          .setTaskName("upload " + hostname + ":" + name).build();
    }

    @Override
    public boolean count(long count) {
      pb.stepBy(count);
      return true;
    }

    @Override
    public void end() {
      pb.close();
    }
  }

  @Override
  public void put(String sourceFile, String target) throws IOException {
    if(!reconnectedIfNeeded()){
      return;
    }
    ChannelSftp channelSftp = null;
    try {
      Preconditions.checkNotNull(sourceFile);
      Preconditions.checkNotNull(target);
      channelSftp = (ChannelSftp) sess.openChannel("sftp");
      channelSftp.connect();
      Preconditions.checkNotNull(channelSftp);
      channelSftp.put(sourceFile, target, new SFtpDeployMonitor());
    } catch (JSchException | SftpException e) {
      throw new DeployException(e);
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
    }
  }

  @Override
  public List<Pair<String, Long>> ls(String dir) throws IOException {
    if(!reconnectedIfNeeded()){
      return new ArrayList<>();
    }
    ChannelSftp channelSftp = null;
    try {
      channelSftp = (ChannelSftp) sess.openChannel("sftp");
      channelSftp.connect();
      Preconditions.checkNotNull(channelSftp);
      Vector<ChannelSftp.LsEntry> files = channelSftp.ls(dir);
      return files.stream().map(x -> Pair.of(x.getFilename(),
          (long) x.getAttrs().getMTime() * 1000L)).collect(Collectors.toList());
    } catch (JSchException | SftpException e) {
      throw new DeployException(e);
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
    }
  }

  @Override
  public void shutdown() {
    if (sess != null && sess.isConnected()) {
      sess.disconnect();
    }
  }
}
