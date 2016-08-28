package com.devhc.jobdeploy.strategy.build;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.strategy.ITaskStrategy;

public class AntBuild implements ITaskStrategy {

  @Override
  public void run(App app) {
    CmdHelper.execCmd("ant", app.getDeployContext().getBuildDir());
  }
}
