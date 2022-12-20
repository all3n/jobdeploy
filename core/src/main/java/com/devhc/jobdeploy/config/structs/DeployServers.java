package com.devhc.jobdeploy.config.structs;

import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.ssh.JschDriver;
import com.devhc.jobdeploy.ssh.LocalDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeployServers {

  private List<DeployServer> servers = new ArrayList<DeployServer>();
  private DeployJson dc;
  private static final Set<String> SERVER_INNER_KEY_NAMES = Sets.newHashSet(
      "server", "deployto", "chown", "chmod");


  public DeployServers(DeployJson dc) throws Exception {
    this.dc = dc;
    JSONArray jsonServers = dc.getServers();
    int srvCount = jsonServers.length();
    for (int i = 0; i < srvCount; i++) {
      Object srvObj = jsonServers.get(i);
      DeployServer server = new DeployServer();
      if (srvObj.getClass() == String.class) {
        server.setServer((String) srvObj);
        server.setChmod(dc.getChmod());
        server.setChown(dc.getChown());
        server.setDeployto(dc.getDeployTo());
      } else if (srvObj.getClass() == JSONObject.class) {
        JSONObject serverInfo = dc.getServers().getJSONObject(i);
        server.setServer(serverInfo.optString("server"));

        String deployTo = DeployUtils
            .parseRealValue(serverInfo.optString("deployto", ""), dc, dc.getDeployTo());
        server.setDeployto(deployTo);

        server.setChown(serverInfo.optString("chown", dc.getChown()));
        if (StringUtils.isEmpty(server.getChown())) {
          server.setChown(dc.getChown());
        }

        server.setChmod(serverInfo.optString("chmod", dc.getChmod()));
        if (StringUtils.isEmpty(server.getChmod())) {
          server.setChmod(dc.getChmod());
        }
        Map<String, String> args = Maps.newHashMap();
        Iterator ki = serverInfo.keys();
        while (ki.hasNext()) {
          String pk = (String) ki.next();
          if (SERVER_INNER_KEY_NAMES.contains(pk)) {
            continue;
          }
          args.put(pk, serverInfo.getString(pk));
        }
        server.setArgs(args);
      }

      if (!server.getDeployto().startsWith("/")) {
        server.setDeployto("/home/" + dc.getUser() + "/"
            + server.getDeployto());
      }

      if ("".equals(server.getServer())) {
        throw new DeployException("servers[" + i
            + "].server is empty..");
      }

      servers.add(server);
    }
    // init server driver
    for (DeployServer server : ProgressBar.wrap(servers, "connect server")) {
      String hostname = server.getServer();
      DeployDriver driver;
      if (hostname.startsWith("local")) {
        driver = new LocalDriver();
        driver.setDeployJson(dc);
      } else {
        JschDriver sd = new JschDriver(server.getServer(), dc.getUser());
        sd.setProxyServer(dc.getProxy());
        sd.setPassword(dc.getPassword());
        sd.setKeyfile(dc.getKeyFile());
        sd.setKeyfilePass(dc.getKeyFilePass());
        sd.setDeployJson(dc);
        driver = sd;
      }
      driver.init();
      server.setDriver(driver);
      driver.setTimeout(dc.getSshTimeout());
      driver.setSudo(dc.getSudo());
      driver.setColor(AnsiColorBuilder.getRandomColor());
      server.setTmpDir(dc.getRemoteTmpUserDir());
    }
  }

  public void mkdirDeployTmpDir() throws Exception {
    exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server) throws Exception {
        server.getDriver().mkdir(server.getTmpDir(), "777", dc.getChown());
      }
    });
  }

  public void cleanDeployTmpDir() throws Exception {
    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server) throws Exception {
        server.getDriver().execCommand("rm -rf " + server.getTmpDir());
      }
    });
  }

  public void exec(DeployServerExecCallback execImpl) throws Exception {
    if (dc.getParallel() == 1) {
      for (DeployServer server : servers) {
        if (server.driver.isValid()) {
          execImpl.run(dc, server);
        }
      }
    } else {
      System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
          String.valueOf(dc.getParallel()));
      servers.parallelStream().forEach(server -> {
        try {
          if (server.driver.isValid()) {
            execImpl.run(dc, server);
          }
        } catch (Exception e) {
          throw new DeployException(e);
        }
      });
    }
  }

  public void shutdown() {
    for (DeployServer server : servers) {
      server.shutdown();
    }
  }

  public class DeployServer {

    private String server;
    private String chmod;
    private String chown;
    private String deployto;
    private DeployDriver driver;
    private String tmpDir;

    private Map<String, String> args;

    public String getServer() {
      return server;
    }

    public void setServer(String server) {
      this.server = server;
    }

    public String getChmod() {
      return chmod;
    }

    public void setChmod(String chmod) {
      this.chmod = chmod;
    }

    public String getChown() {
      return chown;
    }

    public void setChown(String chown) {
      this.chown = chown;
    }

    public String getDeployto() {
      return deployto;
    }

    public void setDeployto(String deployto) {
      this.deployto = deployto;
    }

    public DeployDriver getDriver() {
      return driver;
    }

    public void setDriver(DeployDriver driver) {
      this.driver = driver;
    }

    public String getTmpDir() {
      return tmpDir;
    }

    public void setTmpDir(String tmpDir) {
      this.tmpDir = tmpDir;
    }

    public Map<String, String> getArgs() {
      return args;
    }

    public void setArgs(Map<String, String> args) {
      this.args = args;
    }

    @Override
    public String toString() {
      return "DeployServer{" +
          "server='" + server + '\'' +
          ", chmod='" + chmod + '\'' +
          ", chown='" + chown + '\'' +
          ", deployto='" + deployto + '\'' +
          '}';
    }

    public void shutdown() {
      driver.shutdown();
    }

    public void initIfNeed() {
      try {
        driver.init();
      } catch (IOException e) {
        driver.setValid(false);
        throw new RuntimeException(e);
      }
    }
  }

  public interface DeployServerExecCallback {

    public void run(DeployJson dc, DeployServer server) throws Exception;
  }

  public int getLength() {
    return servers.size();
  }


  public List<DeployServer> getServers() {
    return servers;
  }
}
