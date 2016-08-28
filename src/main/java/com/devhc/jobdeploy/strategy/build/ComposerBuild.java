package com.devhc.jobdeploy.strategy.build;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.strategy.ITaskStrategy;

/**
 * for php composer build 
 * @author wanghch
 */
public class ComposerBuild implements ITaskStrategy {
  @Override
  public void run(App app) {
    CmdHelper.execCmd("composer install", app.getDeployContext().getBuildDir());
  }
}
