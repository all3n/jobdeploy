package com.devhc.jobdeploy.args;

import com.google.common.collect.Lists;
import com.devhc.jobdeploy.config.Constants;

import java.util.List;

/**
 * deploy [headoptions] [stage]:task [taskOptions]
 * @author wanghch
 *
 */
public class AppArgs {
  private String stage;
  private String task = Constants.DEFAULT_TASK;
  private List<String> subCmds = Lists.newArrayList();

  private List<String> headOptions = Lists.newArrayList();
  private List<String> taskOptions = Lists.newArrayList();

  public String getStage() {
    return stage;
  }

  public String getTask() {
    return task;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public void setStage(String stage) {
    this.stage = stage;
  }

  public List<String> getHeadOptions() {
    return headOptions;
  }

  public void setHeadOptions(List<String> headOptions) {
    this.headOptions = headOptions;
  }

  public List<String> getTaskOptions() {
    return taskOptions;
  }

  public void setTaskOptions(List<String> taskOptions) {
    this.taskOptions = taskOptions;
  }

  public List<String> getSubCmds() {
    return subCmds;
  }

  public void setSubCmd(List<String> subCmds) {
    this.subCmds = subCmds;
  }

  @Override public String toString() {
    return "AppArgs{" +
      "stage='" + stage + '\'' +
      ", task='" + task + '\'' +
      ", subCmd='" + subCmds + '\'' +
      ", headOptions=" + headOptions +
      ", taskOptions=" + taskOptions +
      '}';
  }
}
