package com.devhc.jobdeploy.plugin;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;

public class AppPlugin {
  protected App app;
  public void setApp(App app){
    this.app = app;
  }

  public void onSetup(DeployJson config) {
  }
  public void onRunTask(String taskName){

  }
  public void afterRunTask(String taskName){

  }
  public void onException(Exception e) {
  }

  public void onShutdown() {

  }
}
