package com.devhc.jobdeploy.strategy.upload.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.base.Preconditions;
import java.io.File;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class MavenPackageUpload implements ITaskStrategy {

  private static Logger log = Loggers.get();

  @Override
  public void run(App app) throws Exception {
    DeployJson dc = app.getDeployJson();
    String buildDir = app.getDeployContext().getBuildDir();
    String targetDirPath;
    String curExecDir = FileUtils.getExecDir();
    String uploadFile;
    final String updateFileName;
    String targetJarDirBase = "".equals(buildDir) ? curExecDir : buildDir;
    if (StringUtils.isEmpty(dc.getUploadTarget())) {
      targetDirPath = targetJarDirBase + File.separator + "target";
      File jarPath = new File(targetDirPath);
      String[] tgzList = jarPath.list((dir, name) -> name.endsWith(".jar"));
      Preconditions.checkState(tgzList != null && tgzList.length > 0,
          "jar not found in " + targetDirPath);
      String fileName = tgzList[0];
      updateFileName = fileName;
      uploadFile = targetDirPath + File.separator + fileName;
    } else {
      List<String> files = FileUtils.glob("glob:**/" + dc.getUploadTarget(), targetJarDirBase);
      Preconditions.checkState(files.size() > 0, "jar not found in " + dc.getUploadTarget());
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
      String mv2target;
      mv2target = "cp -f " + tmpUser + "/" + updateFileName + " " + releaseCommitidDir;
      driver.execCommand(mv2target);
      driver.changePermission(releaseCommitidDir, chmod, chown,
          false);
      driver.symlink(releaseCommitidDir, updateFileName, dc1.getLinkJarName());
    });
  }
}
