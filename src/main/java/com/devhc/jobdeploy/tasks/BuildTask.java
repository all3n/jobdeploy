package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wanghch
 */
@DeployTask
public class BuildTask extends JobTask {

  private static Logger log = Loggers.get();
  @Autowired
  DeployJson dc;
  @Autowired
  StrategyManager sm;
  @Autowired
  App app;

  public void exec() throws Exception {
    if (dc.getStrategy() != null) {
      log.info("strategy:{} skip build", AnsiColorBuilder.red(dc.getStrategy().getName()));
    }
  }
}
