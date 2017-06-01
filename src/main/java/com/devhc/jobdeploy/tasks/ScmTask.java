package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.DeployMode;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class ScmTask extends JobTask {
  private static Logger log = Loggers.get();
  @Autowired
  DeployJson dc;

  @Autowired
  DeployContext ctx;

  public void exec() {
    if(dc.getDeployMode() == DeployMode.LOCAL){
      log.info("LOCAL MODE skip scm task");
      return;
    }
    ScmDriver scm = ctx.getScmDriver();
    log.info("srcDir:{} buildDir:{}", ctx.getSrcDir(), ctx.getBuildDir());
    if (StringUtils.isNotEmpty(scm.getRepositoryUrl())) {
      if (scm.scmExists()) {
        log.info("scm start update");
        if(!scm.checkScmDirValid()){
          throw new DeployException("respository url not match ");
        }
        scm.update();
      } else {
        log.info("scm start checkout");
        scm.checkout();
      }
    } else {
      log.info("scm repository url is not set,try use local repository");
      if (scm.scmExists()) {
        log.info("scm start update");
        scm.update();
      }
    }
  }
}
