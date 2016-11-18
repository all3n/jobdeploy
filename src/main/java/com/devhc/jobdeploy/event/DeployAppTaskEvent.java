package com.devhc.jobdeploy.event;

import com.devhc.jobdeploy.JobTask;

import java.util.EventObject;

/**
 * Created by wanghch on 16/11/18.
 */
public class DeployAppTaskEvent extends EventObject {

  private JobTask task;

  public DeployAppTaskEvent(Object source, JobTask task) {
    super(source);
    this.task = task;
  }

  public JobTask getTask() {
    return task;
  }

  public void setTask(JobTask task) {
    this.task = task;
  }
}
