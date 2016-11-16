package com.devhc.jobdeploy.config;

import java.util.List;

/**
 * Created by wanghch on 16/11/11.
 */
public class ScriptTask {
  private String name;
  private String dir;
  private List<String> cmd;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public List<String> getCmd() {
    return cmd;
  }

  public void setCmd(List<String> cmd) {
    this.cmd = cmd;
  }

  @Override public String toString() {
    return "ScriptTask{" +
      "name='" + name + '\'' +
      ", dir='" + dir + '\'' +
      ", cmd=" + cmd +
      '}';
  }
}
