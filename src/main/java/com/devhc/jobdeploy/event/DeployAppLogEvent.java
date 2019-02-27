package com.devhc.jobdeploy.event;

import java.util.EventObject;

/**
 * Created by wanghch on 16/11/18.
 */
public class DeployAppLogEvent extends EventObject {

  private String level;
  private String msg;
  private long taskId;
  private String className;

  public static final String INFO = "INFO";
  public static final String ERROR = "ERROR";
  public static final String SYSOUT = "SYSOUT";

  public DeployAppLogEvent(Object source, String msg, String level, String className) {
    super(source);
    this.msg = msg;
    this.level = level;
    this.className = className;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public long getTaskId() {
    return taskId;
  }

  public void setTaskId(long taskId) {
    this.taskId = taskId;
  }

  public String getClassName() {
    return className;
  }
}
