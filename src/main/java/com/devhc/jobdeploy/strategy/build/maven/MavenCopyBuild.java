package com.devhc.jobdeploy.strategy.build.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.devhc.jobdeploy.utils.MavenCmds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenCopyBuild extends MavenBuild {

  @Override
  public void doMavenAction(App app) {
    String buildDir = app.getDeployContext().getBuildDir();
    CmdHelper.execCmd(MavenCmds.buildJarCopyDep + " " + app.getDeployJson().getMavenParams(),
      buildDir, log);
  }

}
