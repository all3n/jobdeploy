package com.devhc.jobdeploy.config.structs;

import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.DeployUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeployServers {

  private List<DeployServer> servers = new ArrayList<DeployServer>();
  private DeployJson dc;

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
      }

      if (!server.getDeployto().startsWith("/")) {
        server.setDeployto("/home/" + dc.getUser() + "/"
            + server.getDeployto());
      }

      if ("".equals(server.getServer())) {
        throw new DeployException("servers[" + i
            + "].server is empty..");
      }

      SSHDriver driver;

      if (StringUtils.isNotEmpty(dc.getKeyFile()) && StringUtils.isEmpty(dc.getPassword())) {
        driver = new SSHDriver(server.getServer(), dc.getUser(),
            dc.getKeyFile(), dc.getKeyFilePass());
      } else {
        driver = new SSHDriver(server.getServer(), dc.getUser(),
            dc.getPassword());
      }
      driver.setTimeout(dc.getSshTimeout());
      driver.setSudo(dc.getSudo());
      driver.setColor(AnsiColorBuilder.getRandomColor());
      server.setDriver(driver);
      server.setTmpDir(dc.getRemoteTmpUserDir());
      servers.add(server);
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
    for (DeployServer server : servers) {
      execImpl.run(dc, server);
    }
  }

  public class DeployServer {

    private String server;
    private String chmod;
    private String chown;
    private String deployto;
    private SSHDriver driver;
    private String tmpDir;

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

    public SSHDriver getDriver() {
      return driver;
    }

    public void setDriver(SSHDriver driver) {
      this.driver = driver;
    }

    public String getTmpDir() {
      return tmpDir;
    }

    public void setTmpDir(String tmpDir) {
      this.tmpDir = tmpDir;
    }

  }

  public interface DeployServerExecCallback {

    public void run(DeployJson dc, DeployServer server) throws Exception;
  }

  public int getLength() {
    return servers.size();
  }
}
