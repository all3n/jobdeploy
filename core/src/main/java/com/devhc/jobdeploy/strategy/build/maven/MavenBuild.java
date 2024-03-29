package com.devhc.jobdeploy.strategy.build.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.tasks.BuildTask;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.devhc.jobdeploy.utils.MavenCmds;
import java.io.File;
import org.slf4j.Logger;

public abstract class MavenBuild implements ITaskStrategy {

  public static Logger log = Loggers.get(BuildTask.class);

  @Override
  public void run(App app) {
    doMavenClean(app);
    doMavenAction(app);
  }

  public abstract void doMavenAction(App app);

  protected void doMavenClean(App app) {
    String buildDir = app.getDeployContext().getBuildDir();
    File buildDirPomXML = new File(buildDir + File.separator + "pom.xml");
    if (!buildDirPomXML.exists()) {
      log.error(buildDir + File.separator + "pom.xml is not exists! ");
      throw new DeployException("pom.xml is not exists");
    }
    String mvn = app.getConfig().getMaven();
    CmdHelper.execCmd(mvn + " " + MavenCmds.clean, buildDir, log);
  }

}
