package com.devhc.jobdeploy.strategy.build.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.MavenCmds;

public class MavenInstallBuild extends MavenBuild {

  @Override
  public void doMavenAction(App app) {
    String buildDir = app.getDeployContext().getBuildDir();
    CmdHelper.execCmd(MavenCmds.buildJarAndInstall + " " + app.getDeployJson().getMavenParams(),
      buildDir);
  }

}
