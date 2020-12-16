package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import lombok.Data;
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

    public JschDriver(String hostname, String username) throws IOException {
        this.username = username;
        this.hostname = hostname;
    }

    @Override
    public void init() {
        this.jSch = new JSch();
        boolean isAuthenticated = false;
        if (StringUtils.isNotEmpty(keyfile)) {
            try {
                jSch.addIdentity(keyfile, keyfilePass);
                sess = jSch.getSession(username, hostname);
                sess.setConfig("StrictHostKeyChecking", "no");
                sess.connect();
                isAuthenticated = true;
            } catch (JSchException e) {
                throw new DeployException(e.getMessage());
            }
            Preconditions.checkArgument(isAuthenticated, "key auth fail" + keyfile);
        } else if (StringUtils.isNotEmpty(password)) {
            try {
                sess = jSch.getSession(username, hostname);
                sess.setPassword(password);
                sess.setConfig("StrictHostKeyChecking", "no");
                sess.connect();
                isAuthenticated = true;
            } catch (JSchException e) {
                throw new DeployException(e.getMessage());
            }
            Preconditions.checkArgument(isAuthenticated, "password auth fail");
        } else if (SSHAgentConnector.isConnectorAvailable()) {
            // ssh -A
            try {
                SSHAgentConnector con = new SSHAgentConnector(new JNAUSocketFactory());
                jSch.setIdentityRepository(new RemoteIdentityRepository(con));
                sess = jSch.getSession(username, hostname);
                sess.setConfig("StrictHostKeyChecking", "no");
                sess.connect();
                isAuthenticated = true;
            } catch (AgentProxyException | JSchException e) {
                throw new DeployException(e.getMessage());
            }
        } else if (PageantConnector.isConnectorAvailable()) {
            // for putty
            try {
                PageantConnector con = new PageantConnector();
                jSch.setIdentityRepository(new RemoteIdentityRepository(con));
                sess = jSch.getSession(username, hostname);
                sess.setConfig("StrictHostKeyChecking", "no");
                sess.connect();
                isAuthenticated = true;
            } catch (AgentProxyException | JSchException e) {
                throw new DeployException(e.getMessage());
            }
        } else {
        }
    }

    @Override
    public void execCommand(String command) {
        ChannelExec ce = null;
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
        }finally {
          if(ce != null) {
              ce.disconnect();
          }
        }

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
            channelSftp.put(sourceFile, target);
        } catch (JSchException | SftpException e) {
            throw new DeployException(e);
        }finally {
            if(channelSftp != null){
                channelSftp.disconnect();
            }
        }
    }

    @Override
    public List<Pair<String, Long>> ls(String dir) throws IOException {
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
      if(sess != null){
          sess.disconnect();
      }
    }
}
