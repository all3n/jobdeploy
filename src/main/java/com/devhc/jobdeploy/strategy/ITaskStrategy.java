package com.devhc.jobdeploy.strategy;

import com.devhc.jobdeploy.App;

public interface ITaskStrategy {
  public void run(App app) throws Exception;
}
