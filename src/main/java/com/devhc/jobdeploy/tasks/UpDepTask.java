package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SCPClient;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.manager.CompressManager;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

@DeployTask
public class UpDepTask extends JobTask {
  @Autowired
  DeployJson dc;

  @Autowired
  CompressManager cm;

  @Autowired
  App app;

  private static Logger log = LoggerFactory.getLogger(UpDepTask.class);

  public void exec() throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    String buildDir = app.getDeployContext().getBuildDir();
    final String depJarFile = "lib.tgz";
    final String tgzFilePath = buildDir + "/" + depJarFile;
    cm.createTgz(buildDir + "/lib", tgzFilePath, "");

    String curExecDir;
    curExecDir = FileUtils.getExecDir();
    if (!".".equals(buildDir)) {
      curExecDir = curExecDir + "/" + buildDir;
    }

    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server)
        throws Exception {
        SSHDriver driver = server.getDriver();
        SCPClient scpClient = driver.getScpClient();
        String release = server.getDeployto() + "/" + scm.getReleseDir();
        driver.mkdir(release, server.getChmod(), server.getChown());

        driver.mkdir(server.getDeployto(), server.getChmod(),
          server.getChown());
        log.info("upload :" + tgzFilePath);

        String tmpUser = app.getDeployContext().getRemoteTmp();
        scpClient.put(tgzFilePath, tmpUser);

        String unzipJars = " tar -zmxvf " + tmpUser + "/" + depJarFile
          + " -C " + release;
        log.info(unzipJars);
        driver.execCommand(unzipJars);
        driver.changePermission(server.getDeployto(),
          server.getChmod(), server.getChown(), true);

        String rmJar = " rm -rf " + depJarFile;
        driver.execCommand(rmJar);
      }
    });
    File tgzFile = new File(tgzFilePath);
    tgzFile.delete();
  }
}
