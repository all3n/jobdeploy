package com.devhc.jobdeploy.strategy;

import com.devhc.jobdeploy.App;

public interface ITaskStrategy {

   void run(App app) throws Exception;
}
