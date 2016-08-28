package com.devhc.jobdeploy.scm.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeploySShSessionFactory extends JschConfigSessionFactory {
  private static Logger log = LoggerFactory.getLogger(DeploySShSessionFactory.class);
  private String sshKeyFilePath;
  private String sshKeyPassword;

  @Override
  protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
    JSch jsch = new JSch();
    jsch.removeAllIdentity();
    log.info("git use key:{}", sshKeyFilePath);
    if (StringUtils.isEmpty(sshKeyPassword)) {
      jsch.addIdentity(sshKeyFilePath);
    } else {
      jsch.addIdentity(sshKeyFilePath, sshKeyPassword);
    }
    return jsch;
  }

  @Override
  protected void configure(Host hc, Session session) {
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);

  }

  public String getSshKeyFilePath() {
    return sshKeyFilePath;
  }

  public void setSshKeyFilePath(String sshKeyFilePath) {
    this.sshKeyFilePath = sshKeyFilePath;
  }

  public String getSshKeyPassword() {
    return sshKeyPassword;
  }

  public void setSshKeyPassword(String sshKeyPassword) {
    this.sshKeyPassword = sshKeyPassword;
  }

  public DeploySShSessionFactory(String sshKeyFilePath, String sshKeyPassword) {
    super();
    this.sshKeyFilePath = sshKeyFilePath;
    this.sshKeyPassword = sshKeyPassword;
  }

  public DeploySShSessionFactory() {
  }

}
