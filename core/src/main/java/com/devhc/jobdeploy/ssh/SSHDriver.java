package com.devhc.jobdeploy.ssh;

import ch.ethz.ssh2.*;
import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMStructure;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.spotify.sshagentproxy.AgentProxies;
import com.spotify.sshagentproxy.AgentProxy;
import com.spotify.sshagentproxy.Identity;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
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

    public void auth() throws IOException {
        boolean isAuthenticated = false;
        if (StringUtils.isNotEmpty(keyfile)) {
            isAuthenticated = conn.authenticateWithPublicKey(username, new File(keyfile), keyfilePass);
            Preconditions.checkArgument(isAuthenticated, "key auth fail" + keyfile);
        } else if (StringUtils.isNotEmpty(password)) {
            isAuthenticated = conn.authenticateWithPassword(username,
                    password);
            Preconditions.checkArgument(isAuthenticated, "password auth fail");
        } else if (System.getenv().containsKey(AGENT_SOCK_ENV)) {
            log.info("auth with AgentForard");
            char[] bData = getKeyBytes();
            isAuthenticated = conn.authenticateWithPublicKey(username, bData, keyfilePass);
            Preconditions.checkArgument(isAuthenticated, "agent auth fail");
        }
    }

    public char[] getKeyBytes() {
        final byte[] dataToSign = {0xa, 0x2, (byte) 0xff};
        final AgentProxy agentProxy = AgentProxies.newInstance();
        char[] ret = null;
        try {
            final List<Identity> identities = agentProxy.list();
            for (final Identity identity : identities) {
                if (identity.getPublicKey().getAlgorithm().equals("RSA")) {
                    System.out.println(identity.getComment());
//                    byte[] bdata = agentProxy.sign(identity, dataToSign);
                    byte[] blob = identity.getKeyBlob();
//                    for(int j=0; j<blob.length; j++){
//                        System.out.print(Integer.toHexString(blob[j]&0xff)+":");
//                    }
//                    System.out.println("");
                    PKCS8EncodedKeySpec pkey = new PKCS8EncodedKeySpec(blob);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PrivateKey k = kf.generatePrivate(pkey);
                    System.out.println(Base64.getEncoder().encode(k.getEncoded()));

                    return new String(identity.getKeyBlob()).toCharArray();
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return ret;
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
