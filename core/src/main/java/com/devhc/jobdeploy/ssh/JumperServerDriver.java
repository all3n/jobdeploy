package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.HTopGenerator;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.GlobMatch;
import expect4j.matches.RegExpMatch;

import expect4j.matches.TimeoutMatch;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;

public class JumperServerDriver extends JschDriver {

  private static Logger log = Loggers.get();
  private String jumpGateway;
  private Integer jumperGatewayPort;
  private String jumperSecretPrefix;
  private HTopGenerator codeGenerator;
  private Expect4j expect;
  protected ChannelShell shell;
  private ChannelExec exec;
  private InputStream inputStream;
  private String sftpPrefix;
  private String sshLogin;
  private StringBuilder currentLine = new StringBuilder();

  public String getJumpGateway() {
    return jumpGateway;
  }

  public void setJumpGateway(String jumpGateway) {
    this.jumpGateway = jumpGateway;
  }

  public Integer getJumperGatewayPort() {
    return jumperGatewayPort;
  }

  public void setJumperGatewayPort(Integer jumperGatewayPort) {
    this.jumperGatewayPort = jumperGatewayPort;
  }

  public String getJumperSecretPrefix() {
    return jumperSecretPrefix;
  }

  public void setJumperSecretPrefix(String jumperSecretPrefix) {
    this.jumperSecretPrefix = jumperSecretPrefix;
  }

  public HTopGenerator getCodeGenerator() {
    return codeGenerator;
  }

  public void setCodeGenerator(HTopGenerator codeGenerator) {
    this.codeGenerator = codeGenerator;
  }

  public JumperServerDriver(String hostname, String username) throws IOException {
    super(hostname, username);
  }

  public String getSftpPrefix() {
    return sftpPrefix;
  }

  public void setSftpPrefix(String sftpPrefix) {
    this.sftpPrefix = sftpPrefix;
  }

  @Override
  public void init() throws JSchException {
    Preconditions.checkNotNull(password, "ssh password must not be null");
    log.info("{} use jump gateway: {}:{}", hostname, jumpGateway, jumperGatewayPort);
    this.jSch = new JSch();
    sess = jSch.getSession(username, jumpGateway, jumperGatewayPort);
    sess.setConfig("StrictHostKeyChecking", "no");
    String code;
    if (codeGenerator == null) {
      Scanner scanner = new Scanner(System.in);
      log.error("htop gen secret empty!");
      log.info("please input code manual:");
      code = scanner.nextLine();
    } else {
      code = codeGenerator.genCode();
    }
    String dynamicCode = jumperSecretPrefix + code;
    sess.setPassword(dynamicCode);
    sess.connect(30000);
    this.exec = new ChannelExec();

    this.shell = (ChannelShell) sess.openChannel("shell");
    try {
      shell.setPty(true);
      shell.setPtyType("vt102");
      // reset pty size avoid long log line wrap
      shell.setPtySize(300, 24, 640, 480);
      this.expect = new Expect4j(shell.getInputStream(), shell.getOutputStream()) {
        public void close() {
          super.close();
          sess.disconnect();
        }
      };
      expect.registerBufferChangeLogger((newData, numChars) -> {
        for (int i = 0; i < numChars; ++i) {
          if (newData[i] == '\r' || newData[i] == '\n') {
            if (currentLine.length() > 0) {
              log.info("{}", currentLine);
              currentLine.setLength(0);
            }
          } else {
            currentLine.append(newData[i]);
          }
        }
      });
      shell.connect(3 * 1000);
      expect.expect("Opt");
      expect.send(hostname + "\r");
      int match;
      final String ret[] = new String[2];
      match = expect.expect(Arrays.asList(
              new GlobMatch("username", null),
              new RegExpMatch("(\\d+)\\s+\\|\\s+(\\S+-ssh-public-key-user)", state -> {
                ret[0] = state.getMatch(1);
                ret[1] = state.getMatch(2);
              }),
              new RegExpMatch("(\\d+)\\s+\\| root-system-user", state -> {
                ret[0] = state.getMatch(1);
                ret[1] = "root-system-user";
              }),
              new GlobMatch("复用SSH连接", null),
              new GlobMatch("开始连接到", null)
          )
      );

      //log.info("{}:{}", hostname, match);
      int match2 = -1;
      if (match == 0) {
        expect.send(username + "\r");
      } else if (match == 1) {
        expect.send(ret[0] + "\r");
        sshLogin = ret[1];
      } else if (match == 2) {
        sshLogin = ret[1];
        expect.send(ret[0] + "\r");
        match2 = expect.expect(Arrays.asList(
            new GlobMatch("username", null),
            new GlobMatch("password", null),
            new TimeoutMatch(null)
        ));
        if (match2 == 0) {
          expect.send(username + "\r");
        }
      } else if (match == 3 || match == 4) {
        // skip user/password
        match = 3;
      } else {
        throw new DeployException(match + " invald match result");
      }

      if (match != 3 && match2 != 1) {
        match = expect.expect(Arrays.asList(
            new GlobMatch("password", null),
            new GlobMatch("复用SSH连接", null),
            new TimeoutMatch(null)
        ));
        if (match == 0) {
          log.info("Input Password:" + StringUtils.repeat("*", password.length()));
          expect.send(password + "\r");
        } else {
          // reuse ssh connect
        }
      }

      if (StringUtils.isNotEmpty(sftpPrefix) && !sftpPrefix.endsWith("/")) {
        sftpPrefix += "/";
      }
      // add wait
      expect.expect("$");
      // skip some strange case will skip first command
      execCommand("echo login");
      valid = true;
    } catch (Exception e) {
      valid = false;
      throw new RuntimeException(e);
    }
  }

  @Override
  public void execCommand(String command) {
    try {
      expect.send(command + "\r");
      // TODO if shell modify shell PS1 would be fail
      expect.expect("$");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  String getSftpPath(String path) {
    if (StringUtils.isEmpty(sftpPrefix)) {
      return path;
    }
    String sftpPath;
    String login = sshLogin == null ? "" : "/" + sshLogin;
    if (path.startsWith("/")) {
      sftpPath = sftpPrefix + hostname + login + path;
    } else {
      sftpPath = sftpPrefix + hostname + login + "/home/" + username + "/" + path;
    }
    return sftpPath;
  }

  @Override
  public void put(String sourceFile, String target) throws IOException {
    ChannelSftp channelSftp = null;
    try {
      Preconditions.checkNotNull(sourceFile);
      Preconditions.checkNotNull(target);
      channelSftp = (ChannelSftp) sess.openChannel("sftp");
      channelSftp.connect();
      Preconditions.checkNotNull(channelSftp);
      String sTarget = getSftpPath(target);
      log.info("upload {} to {}", sourceFile, sTarget);
      channelSftp.put(sourceFile, sTarget, new SFtpDeployMonitor());
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
    String jdir = getSftpPath(dir);
    ChannelSftp channelSftp = null;
    try {
      channelSftp = (ChannelSftp) sess.openChannel("sftp");
      channelSftp.connect();
      Preconditions.checkNotNull(channelSftp);
      log.info("list {}", jdir);
      Vector<LsEntry> files = channelSftp.ls(jdir);
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

  public ChannelShell getShell() {
    return shell;
  }

  public Expect4j getExpect() {
    return expect;
  }

  @Override
  public void changeUser(String user) {
    try {
      expect.send(String.format("sudo -S -u %s bash\r", user));
      int match = expect.expect(Arrays.asList(
          new GlobMatch("password for", null),
          new GlobMatch("\\$", null),
          new TimeoutMatch(null)
      ));
      if (match == 0) {
        expect.send(password + "\r");
        expect.expect("\\$");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
