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
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import java.io.File;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class UpDepTask extends JobTask {

  @Autowired
  DeployJson dc;

  @Autowired
  CompressManager cm;

  @Autowired
  App app;

  private static Logger log = Loggers.get();

  public void exec() throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    String buildDir = app.getDeployContext().getBuildDir();
    final String depJarFile = "lib.tgz";
    final String tgzFilePath = buildDir + File.separator + depJarFile;
    cm.createTgz(buildDir + File.separator + "lib", tgzFilePath, "");

    String curExecDir;
    curExecDir = FileUtils.getExecDir();
    if (!".".equals(buildDir)) {
      curExecDir = curExecDir + File.separator + buildDir;
    }

    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server)
              throws Exception {
        DeployDriver driver = server.getDriver();

        String deployTo = server.getDeployto();
        String chmod = server.getChmod();
        String chown = server.getChown();

        String release = deployTo + "/" + app.getDeployContext().getReleseDir();
        driver.mkdir(release, chmod, chown);

        driver.mkdir(deployTo, chmod, chown);
        log.info("upload :" + tgzFilePath);

        String tmpUser = app.getDeployContext().getRemoteTmp();
        driver.put(tgzFilePath, tmpUser);

        String unzipJars = " tar -zpmxvf " + tmpUser + "/" + depJarFile
            + " -C " + release;
        log.info(unzipJars);
        driver.execCommand(unzipJars);
        driver.changePermission(deployTo, chmod, chown, true);

        String rmJar = " rm -rf " + depJarFile;
        driver.execCommand(rmJar);
      }
    });
    File tgzFile = new File(tgzFilePath);
    tgzFile.delete();
  }
}
