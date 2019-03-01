package com.devhc.jobdeploy;

import com.devhc.jobdeploy.exception.DeployException;

public enum DeployMode {
  LOCAL("local"),
  LATEST("latest");

  private String name;

  DeployMode(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static DeployMode parse(String parseName) {
    for (DeployMode v : DeployMode.values()) {
      if (v.getName().equals(parseName)) {
        return v;
      }
    }
    throw new DeployException("invalid deploy mode :" + parseName);
  }
}
