package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.scm.ScmDriver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wanghch
 *
 */
@DeployTask
public class SymlinkTask extends JobTask {
  private static Logger log = LoggerFactory.getLogger(SymlinkTask.class);
  @Autowired
  DeployJson dc;
  @Autowired
  StrategyManager sm;
  @Autowired
  App app;

  public void exec() throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    // release dir
    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server)
        throws Exception {
        String releaseDir = server.getDeployto() + scm.getReleseDir();
        String currentPath = server.getDeployto() + Constants.REMOTE_CURRENT_DIR;
        String symlinkCmd = "ln -sfT " + releaseDir + " " + currentPath;
        server.getDriver().execCommand(symlinkCmd);

        if (StringUtils.isNotEmpty(dc.getCurrentLink())) {
          String symlinkCurrentCmd = "ln -sfT " + currentPath + " " + dc.getCurrentLink();
          server.getDriver().execCommand(symlinkCurrentCmd);
        }
      }
    });

  }
}
