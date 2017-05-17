package com.devhc.jobdeploy.strategy.build;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.tasks.BuildTask;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class CustomBuild implements ITaskStrategy {
  public static Logger log = Loggers.get(BuildTask.class);

  @Override
  public void run(App app) {
    String customBuild = app.getDeployJson().getCustomBuild();
    if (StringUtils.isEmpty(customBuild)) {
      throw new DeployException("custom build cmd is empty");
    }
    CmdHelper.execCmd(customBuild, app.getDeployContext().getBuildDir(), log);
  }
}
