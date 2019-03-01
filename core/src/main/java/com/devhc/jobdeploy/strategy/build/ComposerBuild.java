package com.devhc.jobdeploy.strategy.build;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.tasks.BuildTask;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;

/**
 * for php composer build
 *
 * @author wanghch
 */
public class ComposerBuild implements ITaskStrategy {

  public static Logger log = Loggers.get(BuildTask.class);

  @Override
  public void run(App app) {
    CmdHelper.execCmd("composer install", app.getDeployContext().getBuildDir(), log);
  }
}
