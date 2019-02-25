package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.DeployMode;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployCustomConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * this task is used for clean some tmp build dir
 *
 * @author wanghch
 */
@DeployTask
public class FinishedTask extends JobTask {

  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  @Autowired
  DeployContext deployContext;

  @Autowired
  DeployCustomConfig customConfig;

  private static Logger log = Loggers.get();

  public void exec() throws Exception {
    if (dc.getDeployMode() == DeployMode.LATEST) {
      final ScmDriver driver = deployContext.getScmDriver();
      String srcDir = app.getDeployContext().getSrcDir();
      // create REVISION in release dir,the content of file is commitid
      final String commitId = driver.getCommitId();
      dc.getDeployServers().exec(new DeployServers.DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServers.DeployServer server) throws Exception {
          String deployTo = server.getDeployto();
          String relaseDir = deployTo + "/" + deployContext.getReleseDir();
          String tmpRevisionFile = deployContext.getRemoteTmp() + "/" + Constants.CURRENT_REVISION;
          server.getDriver().execCommand("echo \"" + commitId + "\">" + tmpRevisionFile);
          server.getDriver().execCommand(
              "mv " + tmpRevisionFile + " " + relaseDir + "/" + Constants.CURRENT_REVISION);
        }
      });

      String tempDir = srcDir;
      log.info("clean up {}", tempDir);
      CmdHelper.execCmd("rm -rf "+tempDir, app.getDeployContext().getBuildDir(), log);

    }
    if (customConfig.isExists()) {
      log.info("save custom to file");
      customConfig.storeToFile();
    }
  }
}
