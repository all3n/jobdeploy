package com.devhc.jobdeploy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Loggers extends SecurityManager {
  private Loggers() {
  }

  public static Logger get() {
    return LoggerFactory.getLogger(Loggers.Singleton.INSTANCE.getClassContext()[1]);
  }

  public static Logger get(String name) {
    return LoggerFactory.getLogger(name);
  }

  public static Logger get(Class clazz) {
    return LoggerFactory.getLogger(clazz);
  }

  private static final class Singleton {
    private static final Loggers INSTANCE = new Loggers();

    private Singleton() {
    }
  }
}
