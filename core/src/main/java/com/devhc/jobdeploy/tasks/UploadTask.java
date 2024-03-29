
package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@DeployTask
public class UploadTask extends JobTask {

  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  private static Logger log = Loggers.get();

  public void exec() throws Exception {
    uploadForArchive(dc);
  }

  private void uploadForArchive(DeployJson dc) throws Exception {
    String buildDir = app.getDeployContext().getBuildDir();
    String targetDirPath;
    String curExecDir = FileUtils.getExecDir();
    String uploadFile;
    final String updateFileName;
    String targetJarDirBase = "".equals(buildDir) ? curExecDir : buildDir;
    if (StringUtils.isEmpty(dc.getUploadTarget())) {
      targetDirPath = targetJarDirBase + File.separator + "target";
      File jarPath = new File(targetDirPath);
      String formatArg = dc.getStrategy().getArgs();
      if (formatArg == null) {
        formatArg = "tar.gz";
      }
      final String formatArgFinal = formatArg;
      String[] tgzList = jarPath.list((dir, name) -> name.endsWith(formatArgFinal));
      Preconditions.checkState(tgzList != null && tgzList.length > 0,
          "tgz not found in " + targetDirPath);
      String fileName = tgzList[0];
      updateFileName = fileName;
      uploadFile = targetDirPath + File.separator + fileName;
    } else {
      List<String> files = FileUtils.glob("glob:**/" + dc.getUploadTarget(), targetJarDirBase);
      Preconditions.checkState(files.size() > 0, "tgz not found in " + dc.getUploadTarget());
      File fuF = new File(files.get(0));
      uploadFile = files.get(0);
      updateFileName = fuF.getName();
    }
    String finalUploadFile = uploadFile;
    dc.getDeployServers().exec((dc1, server) -> {
      String hostname = server.getServer();
      log.debug("server:" + hostname + " deploy..");
      String deployTo = server.getDeployto();
      String releaseCommitidDir = deployTo + "/" + app.getDeployContext().getReleseDir();
      // handle local protocal
      String chmod = server.getChmod();
      String chown = server.getChown();
      String tmpUser = app.getDeployContext().getRemoteTmp();
      String release = deployTo + "/" + Constants.REMOTE_RELEASE_DIR;
      DeployDriver driver = server.getDriver();
      driver.put(finalUploadFile, tmpUser);
      if (dc.getSudoUser() != null) {
        driver.changeUser(dc.getSudoUser());
      }
      driver.mkdir(deployTo, chmod, chown);
      driver.mkdir(release, chmod, chown);
      driver.mkdir(releaseCommitidDir, chmod, chown);
      log.info(AnsiColorBuilder.green("start to upload " + finalUploadFile + " to " + hostname));
      String cmd = CmdHelper.buildUncompressCmd(tmpUser + "/" + updateFileName, releaseCommitidDir);
      driver.execCommand(cmd);
    });
  }
}
