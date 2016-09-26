package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SCPClient;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.exception.DeployException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static Logger log = LoggerFactory.getLogger(UploadTask.class);

  public void exec() throws Exception {
    uploadForJar(dc);
  }

  private void uploadForJar(DeployJson dc) throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    String buildDir = app.getDeployContext().getBuildDir();
    String targetJarDirPath;
    String curExecDir = FileUtils.getExecDir();
    if (".".equals(buildDir)) {
      targetJarDirPath = curExecDir + "/target";
    } else {
      targetJarDirPath = buildDir + "/target";
    }

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
      log.info("target dir has {} jars,choose largest size jar:{}", files.size(), AnsiColorBuilder.green(jarName));
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

    final String uploadFile = targetJarDirPath + "/" + fileName;
    final String updateFileName = fileName;
    final String finalJarName = jarName;
    dc.getDeployServers().exec(new DeployServers.DeployServerExecCallback() {

      @Override
      public void run(DeployJson dc, DeployServers.DeployServer server)
        throws Exception {
        String hostname = server.getServer();
        log.info("server:" + hostname + " deploy..");
        // handle local protocal
        if (hostname.startsWith("local:")) {
          String realpath = server.getServer().substring(6);
          File jarFileObj = new File(uploadFile);
          FileUtils.copyFileToDir(jarFileObj, realpath);
          return;
        }
        String deployTo = server.getDeployto();
        String chown = server.getChown();
        String chmod = server.getChmod();
        SSHDriver driver = server.getDriver();

        driver.mkdir(deployTo, chmod, chown);

        String release = deployTo + "/" + Constants.REMOTE_RELEASE_DIR;
        driver.mkdir(release, chmod, chown);
        String releaseCommitidDir = deployTo + "/" + scm.getReleseDir();
        driver.mkdir(releaseCommitidDir, chmod, chown);

        SCPClient scpClient = driver.getScpClient();
        String tmpUser = app.getDeployContext().getRemoteTmp();
        log.info(AnsiColorBuilder.green("start to upload " + uploadFile + " to "
          + hostname));
        scpClient.put(uploadFile, tmpUser);

        if (updateFileName.endsWith("jar")) {
          String mv2target;
          mv2target = "mv -f " + tmpUser + "/" + updateFileName + " " + releaseCommitidDir;
          driver.execCommand(mv2target);
          driver.changePermission(releaseCommitidDir, chmod, chown,
            false);
          driver.symlink(releaseCommitidDir,updateFileName,dc.getLinkJarName());
        } else if (updateFileName.endsWith("tgz") || updateFileName.endsWith("tar.gz")) {
          String unzipCmd = "tar -zmxvf " + tmpUser + "/" + updateFileName + " -C " + releaseCommitidDir;
          driver.execCommand(unzipCmd);
          driver.symlink(releaseCommitidDir,updateFileName,dc.getLinkJarName());
        }
      }
    });

  }
}
