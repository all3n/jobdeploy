package com.devhc.jobdeploy.strategy.build.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.MavenCmds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class MavenBuild implements ITaskStrategy {
  public static Logger log = LoggerFactory.getLogger(MavenBuild.class);

  @Override
  public void run(App app) {
    doMavenClean(app);
    doMavenAction(app);
  }

  public abstract void doMavenAction(App app);

  protected void doMavenClean(App app) {
    String buildDir = app.getDeployContext().getBuildDir();
    File buildDirPomXML = new File(buildDir + "/pom.xml");
    if (!buildDirPomXML.exists()) {
      log.error(buildDir + "/pom.xml is not exists! ");
      throw new DeployException("pom.xml is not exists");
    }
    CmdHelper.execCmd(MavenCmds.clean, buildDir);
  }

}
