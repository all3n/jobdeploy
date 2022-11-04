package com.devhc.jobdeploy.config;

import com.devhc.jobdeploy.utils.ConfigFile;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;

@Component
public class DeployConfig extends ConfigFile {

  private static final long serialVersionUID = 4822043442950861585L;

  public String getAzkabanUrl() {
    return getProperty("azkaban.url");
  }

  public String getTmpDir() {
    return getProperty("local.tmp.dir");
  }

  public String getMaven() {
    String defaultMaven = SystemUtils.IS_OS_WINDOWS ? "mvn.cmd" : "mvn";
    return getProperty("maven.bin.path", defaultMaven);
  }
}
