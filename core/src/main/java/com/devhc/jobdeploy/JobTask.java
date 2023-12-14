package com.devhc.jobdeploy;

import com.devhc.jobdeploy.config.Constants;

/**
 * JobTask must has annnotion @DeployTask and must name endWith Task
 *
 * @author wanghch
 */
public abstract class JobTask {
  protected Integer status = Constants.JOB_STATUS_OK;

  public void setup() throws Exception {
    // do nothing if not overvide
  }

  public abstract void exec() throws Exception;

  public void cleanup() throws Exception {
    // do nothing if not overvide
  }

  public Integer getStatus() {
    return status;
  }
}
