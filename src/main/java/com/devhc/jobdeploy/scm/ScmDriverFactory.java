package com.devhc.jobdeploy.scm;

import com.devhc.jobdeploy.DeployContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ScmDriverFactory {
  @Autowired
  ConfigurableApplicationContext context;
  @Autowired
  DeployContext ctx;

  /**
   * git|svn
   * @param scmType
   * @return
   */
  public ScmDriver create(String scmType) {
    String driverName = scmType.toLowerCase() + "ScmDriver";
    ScmDriver scm = context.getBean(driverName, ScmDriver.class);
    return scm;
  }
}
