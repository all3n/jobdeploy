package com.devhc.jobdeploy.strategy.build;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.tasks.BuildTask;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;

public class AntBuild implements ITaskStrategy {
  public static Logger log = Loggers.get(BuildTask.class);

  @Override
  public void run(App app) {
    CmdHelper.execCmd("ant", app.getDeployContext().getBuildDir(), log);
  }
}
