package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.manager.CompressManager;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class UploadScriptTask extends JobTask {

  @Autowired
  DeployJson dc;
  @Autowired
  App app;
  @Autowired
  CompressManager cm;

  private static Logger log = Loggers.get();

  public void exec() throws Exception {
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    final String buildDir = app.getDeployContext().getBuildDir();
    Object uploadObj = dc.getUpload();
    if (uploadObj == null) {
      return;
    }
    final ArrayList<DeployScriptFile> list = scanUploadFiles(dc, uploadObj, buildDir);

    dc.getDeployServers().exec(new DeployServerExecCallback() {

      @Override
      public void run(DeployJson dc, DeployServer server)
              throws Exception {
        DeployDriver driver = server.getDriver();

        String deployTo = server.getDeployto();
        String chmod = server.getChmod();
        String chown = server.getChown();

        driver.mkdir(deployTo, chmod, chown);
        String release = deployTo + "/" + app.getDeployContext().getReleseDir();
        driver.mkdir(release, chmod, chown);

        String tmpUser = app.getDeployContext().getRemoteTmp();
        for (DeployScriptFile dsf : list) {
          File f = dsf.getFile();
          if (f.isFile()) {
            driver.put(f.getPath(), tmpUser);
            String command = "mv " + tmpUser + "/" + f.getName()
                + " " + release;
            driver.execCommand(command);
            driver.changePermission(release + "/" + f.getName(),
                chmod, chown, false);
          } else if (f.isDirectory()) {
            String tgzFileName = dsf.getTargetName() + ".tgz";
            String tgzFilePath = buildDir + "/"
                + tgzFileName;

            cm.createTgz(f.getPath(), tgzFilePath, dsf.getTargetName());

            driver.put(tgzFilePath, tmpUser);
            File tgzFile = new File(tgzFilePath);
            tgzFile.delete();
            String command = "tar -zpmxvf " + tmpUser + "/"
                + tgzFileName + " -C " + release;
            driver.execCommand(command);
            driver.changePermission(release, chmod, chown, true);
          }
        }
        driver.changePermission(deployTo, chmod, chown, true);
      }
    });
  }

  public ArrayList<DeployScriptFile> scanUploadFiles(DeployJson dc,
      Object uploadObj, String buildDir) {
    ArrayList<DeployScriptFile> list = Lists.newArrayList();
    File file;
    if (uploadObj instanceof JSONArray) {
      JSONArray uploadArray = (JSONArray) uploadObj;
      for (int i = 0; i < uploadArray.length(); i++) {
        Object scriptItem = uploadArray.opt(i);
        if (scriptItem instanceof String) {
          String strScriptItem = (String) scriptItem;
          if (strScriptItem.indexOf('*') != -1) {
            file = new File(buildDir);
            File[] files = file.listFiles();
            System.out.println(files);
          }

          if (strScriptItem.startsWith("@")) {
            String strStageScript = strScriptItem.substring(1);
            String stage = dc.getStage();
            File stageDir = new File(
                buildDir + File.separator + strStageScript + File.separator + stage);
            if (stageDir.exists()) {
              list.add(new DeployScriptFile(stageDir, strStageScript));
            } else {
              File scriptDefaultstageDir = new File(
                  buildDir + File.separator + strStageScript + File.separator + "default");
              if (scriptDefaultstageDir.exists()) {
                log.info("{} stage script dir is not exist,use default {}", strStageScript,
                    scriptDefaultstageDir);
                list.add(new DeployScriptFile(scriptDefaultstageDir, strStageScript));
              } else {
                log.warn(AnsiColorBuilder.red(scriptDefaultstageDir.getName() + " is not exist"));
              }

            }

          } else if (strScriptItem.indexOf(":") != -1) {
            String strScriptItemArr[] = strScriptItem.split(":");
            if (strScriptItemArr.length == 2) {
              String stageName = strScriptItemArr[0];
              String scriptName = strScriptItemArr[1];
              File stageDir = new File(
                  buildDir + File.separator + scriptName + File.separator + stageName);
              if (stageDir.exists()) {
                log.info("use stage:{} name:{}", stageName, scriptName);
                list.add(new DeployScriptFile(stageDir, scriptName));
              } else {
                log.warn(AnsiColorBuilder.red("{}:{} not exists"), stageName, scriptName);
              }
            }

          } else {
            file = new File(buildDir + File.separator + strScriptItem);
            if (file.exists()) {
              list.add(new DeployScriptFile(file, file.getName()));
            } else {
              log.warn(AnsiColorBuilder.red(strScriptItem + " not exists"));
            }
          }

        }
      }
    } else {
      String uploadStr = uploadObj.toString();
      file = new File(buildDir + File.separator + uploadStr);
      if (file.exists()) {
        list.add(new DeployScriptFile(file, uploadStr));
      } else {
        log.info(AnsiColorBuilder.red(uploadStr + " not exists"));
      }
    }
    return list;
  }

  public class DeployScriptFile {

    private File file;
    private String targetName;

    public DeployScriptFile(File file, String targetName) {
      super();
      this.file = file;
      this.targetName = targetName;
    }

    public File getFile() {
      return file;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public String getTargetName() {
      return targetName;
    }

    public void setTargetName(String targetName) {
      this.targetName = targetName;
    }
  }
}
