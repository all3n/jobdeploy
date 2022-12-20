package com.devhc.jobdeploy.configs;

import com.devhc.jobdeploy.FlowManager;
import com.devhc.jobdeploy.config.AppConfig;
import com.devhc.jobdeploy.config.DeployJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigs {
  @Autowired
  AppConfig config;
  @Autowired
  DeployJson dj;

  @Bean
  public FlowManager flowManager(){
    return new FlowManager(config.getFlows());
  }

}
