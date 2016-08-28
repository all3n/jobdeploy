package com.devhc.jobdeploy.manager;

import com.devhc.jobdeploy.config.DeployJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StageManager {
  @Autowired
  DeployJson dj;

  public static List<String> getStages() {

    return null;
  }
}
