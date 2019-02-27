package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.log.DeployAppLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Loggers extends SecurityManager {

  private App app;

  private Loggers() {
  }

  public static void init(App app) {
    Singleton.INSTANCE.setApp(app);
  }

  public static Logger get() {
    return new DeployAppLogger(
        LoggerFactory.getLogger(Loggers.Singleton.INSTANCE.getClassContext()[1]),
        Loggers.Singleton.INSTANCE);
  }

  public static Logger get(String name) {
    return new DeployAppLogger(LoggerFactory.getLogger(name), Singleton.INSTANCE);
  }

  public static Logger get(Class clazz) {
    return new DeployAppLogger(LoggerFactory.getLogger(clazz), Singleton.INSTANCE);
  }

  private static final class Singleton {

    private static final Loggers INSTANCE = new Loggers();

    private Singleton() {
    }
  }

  public App getApp() {
    return app;
  }

  public void setApp(App app) {
    this.app = app;
  }
}
