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
import expect4j.Expect4j;
import expect4j.matches.GlobMatch;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
            this.expect = new Expect4j(shell.getInputStream(), shell.getOutputStream()) {
                public void close() {
                    super.close();
                    sess.disconnect();
                }
            };
            expect.registerBufferChangeLogger((newData, numChars) -> {
                String msg = new String(newData, 0, numChars);
                System.out.print(msg);
            });
            shell.connect(3 * 1000);
            expect.expect("Opt");
            expect.send(hostname + "\r\n");
            expect.expect("username");
            expect.send(username + "\r\n");

            int match = expect.expect(Arrays.asList(
                new GlobMatch("password", null),
                new GlobMatch("复用SSH连接", null)
            ));
            if (match == 0) {
                expect.send(password + "\r\n");
            } else {
                // reuse ssh connect
            }

            if (StringUtils.isNotEmpty(sftpPrefix) && !sftpPrefix.endsWith("/")) {
                sftpPrefix += "/";
            }

            valid = true;
        } catch (Exception e) {
            valid = false;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execCommand(String command) {
        try {
            expect.send(command + "\r\n");
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
        if (path.startsWith("/")) {
            sftpPath = sftpPrefix + hostname + path;
        } else {
            sftpPath = sftpPrefix + hostname + "/home/" + username + "/" + path;
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
}
