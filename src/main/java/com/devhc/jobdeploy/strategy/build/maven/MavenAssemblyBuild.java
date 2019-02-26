package com.devhc.jobdeploy.strategy.build.maven;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.MavenCmds;

public class MavenAssemblyBuild extends MavenBuild {

  @Override
  public void doMavenAction(App app) {
    CmdHelper.execCmd(app.getConfig().getMaven()+" "+MavenCmds.buildJarAssembly + " " + app.getDeployJson().getMavenParams(),
      app.getDeployContext().getBuildDir(),log);
  }

}
