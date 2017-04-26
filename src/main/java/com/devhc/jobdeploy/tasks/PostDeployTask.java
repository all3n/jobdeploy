package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.sftp.ErrorCodes;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author wanghch
 *
 */
@DeployTask
public class PostDeployTask extends JobTask {
  private static Logger log = Loggers.get();
  @Autowired
  DeployJson dc;
  @Autowired
  StrategyManager sm;
  @Autowired
  App app;

  public void exec() throws Exception {
    final String postDeployScript = dc.getPostDeployScript();
    if (StringUtils.isEmpty(postDeployScript)) {
      return;
    }
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    // release dir
    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server)
        throws Exception {
        String currentPath = server.getDeployto() + "/" + Constants.REMOTE_CURRENT_DIR;
        String postScript = currentPath + "/" + postDeployScript;
        try {
          SFTPv3FileAttributes stat = server.getDriver().getSftpClient().stat(postScript);
          server.getDriver().execCommand("cd " + currentPath + ";sh " + postScript);
        } catch (SFTPException e) {
          if (e.getServerErrorCode() == ErrorCodes.SSH_FX_NO_SUCH_FILE) {
          } else {
            throw new DeployException(e);
          }
        }
      }
    });

  }
}
