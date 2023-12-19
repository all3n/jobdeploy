package com.devhc.jobdeploy.strategy.upload;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployPattern;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.manager.CompressManager;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.ssh.DeployDriver;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.google.common.io.Files;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.utils.Lists;
import org.json.JSONArray;

public class CodeUpload implements ITaskStrategy {

  @Override
  public void run(final App app) throws Exception {
    String buildDir = app.getDeployContext().getBuildDir();

    File f = new File(buildDir);
    FileOutputStream fOut = null;
    OutputStream out = null;
    final ScmDriver scm = app.getDeployContext().getScmDriver();
    try {

    } catch (Exception e) {
      e.printStackTrace();
      throw new DeployException(e.getMessage());
    } finally {
      IOUtils.closeQuietly(out);
    }
    CompressManager cm = app.getDeployContext().getCompressManager();
    File tgzFile = Files.createTempDir();
    final String tgzFileName = "code.tgz";
    final String tgzFilePath = tgzFile + File.separator + tgzFileName;
    DeployPattern dp = new DeployPattern(app.getDeployJson().getObject("code_patterns"));
    try {
      cm.createTgz(app.getDeployContext().getSrcDir(), tgzFilePath, "code", dp);
    } catch (IOException e) {
      e.printStackTrace();
    }

    final String scmDirName = app.getDeployContext().getScmDriver().getScmDirName();


    app.getDeployJson().getDeployServers()
            .exec(new DeployServers.DeployServerExecCallback() {
              @Override
              public void run(DeployJson dc, DeployServers.DeployServer server)
                      throws Exception {

                String deployTo = server.getDeployto();
                String chmod = server.getChmod();
                String chown = server.getChown();
                DeployDriver driver = server.getDriver();
                String tmpUser = app.getDeployContext().getRemoteTmp();
                driver.put(tgzFilePath, tmpUser);

                String release = deployTo + "/"
                        + app.getDeployContext().getReleseDir();
                driver.mkdir(release, chmod, chown);
                String command = "tar -zpmxf " + tmpUser + "/"
                        + tgzFileName + " --strip-components 1 -C " + release;
                driver.execCommand(command);
                driver.execCommand("rm " + tmpUser + "/" + tgzFileName);
                driver.execCommand("rm -rf " + release + "/" + scmDirName);
              }
            });

    File tgzFilePathFile = new File(tgzFilePath);
    tgzFilePathFile.delete();
    tgzFile.delete();
  }
}
