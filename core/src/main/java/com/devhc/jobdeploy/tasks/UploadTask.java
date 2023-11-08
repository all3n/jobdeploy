
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
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
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
    uploadForJar(dc);
  }

  private void uploadForJar(DeployJson dc) throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    String buildDir = app.getDeployContext().getBuildDir();
    String targetJarDirPath;
    String curExecDir = FileUtils.getExecDir();
    String uploadFile;
    final String updateFileName;
    final String finalJarName;

    String targetJarDirBase;
    if (".".equals(buildDir)) {
      targetJarDirBase = curExecDir;
    } else {
      targetJarDirBase = buildDir;
    }

    if (StringUtils.isEmpty(dc.getUploadTarget())) {
      targetJarDirPath = targetJarDirBase + File.separator + "target";

      String strategy = dc.getStrategy().getName();

      File jarPath = new File(targetJarDirPath);
      String jarName = "";

      File[] jarList = jarPath.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("jar");
        }
      });
      if (jarList == null) {
        throw new DeployException("mvn target dir is not exist,please check build");
      } else if (jarList.length == 0) {
        throw new DeployException("jar build failed");
      } else if (jarList.length == 1) {
        jarName = jarList[0].getName();
      } else {
        // length > 1
        List<File> files = Arrays.asList(jarList);
        Collections.sort(files, new Comparator<File>() {
          @Override
          public int compare(File o1, File o2) {
            return (int) Math.signum(o2.length() - o1.length());
          }
        });
        jarName = files.get(0).getName();
        log.info("target dir has {} jars,choose largest size jar:{}", files.size(),
            AnsiColorBuilder.green(jarName));
      }
      String fileName = jarName;

      if ("maven:assembly:archive".equals(strategy)) {
        String formatArg = dc.getStrategy().getArgs();
        if (formatArg == null) {
          formatArg = "tar.gz";
        }
        final String formatArgFinal = formatArg;
        String[] tgzList = jarPath.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith(formatArgFinal);
          }
        });
        fileName = tgzList[0];
      } else {
        fileName = jarName;
      }

      uploadFile = targetJarDirPath + File.separator + fileName;
      updateFileName = fileName;
      finalJarName = jarName;
    } else {
      List<String> files = FileUtils.glob("glob:**/" + dc.getUploadTarget(), targetJarDirBase);
      System.out.println(files);
      File fuF = new File(files.get(0));
      uploadFile = files.get(0);
      updateFileName = fuF.getName();
      finalJarName = "";
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
      if(dc.getSudoUser() != null){
        driver.changeUser(dc.getSudoUser());
      }
      driver.mkdir(deployTo, chmod, chown);
      driver.mkdir(release, chmod, chown);
      driver.mkdir(releaseCommitidDir, chmod, chown);

      log.info(AnsiColorBuilder.green("start to upload " + finalUploadFile + " to " + hostname));

      if (updateFileName.endsWith("jar")) {
        String mv2target;
        mv2target = "cp -f " + tmpUser + "/" + updateFileName + " " + releaseCommitidDir;
        driver.execCommand(mv2target);
        driver.changePermission(releaseCommitidDir, chmod, chown,
            false);
        driver.symlink(releaseCommitidDir, updateFileName, dc1.getLinkJarName());
      } else if (updateFileName.endsWith("tgz") || updateFileName.endsWith("tar.gz")) {
        String unzipCmd =
            "tar -zpmxf " + tmpUser + "/" + updateFileName + " -C " + releaseCommitidDir;
        driver.execCommand(unzipCmd);
//        driver.execCommand("rm -rf " + tmpUser);
        if (StringUtils.isNotEmpty(finalJarName)) {
          driver.symlink(releaseCommitidDir, finalJarName, dc1.getLinkJarName());
        }
      }
    });

  }
}
