package com.devhc.jobdeploy;

/**
 * JobTask must has annnotion @DeployTask 
 * and must name endWith Task
 * @author wanghch
 *
 */
public abstract class JobTask {
  public void setup() throws Exception {
    // do nothing if not overvide
  }

  public abstract void exec() throws Exception;

  public void cleanup() throws Exception {
    // do nothing if not overvide
  }
}
