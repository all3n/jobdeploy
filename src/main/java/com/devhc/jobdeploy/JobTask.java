package com.devhc.jobdeploy;

import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;

/**
 * JobTask must has annnotion @DeployTask 
 * and must name endWith Task
 * @author wanghch
 *
 */
public abstract class JobTask {
  protected static Logger log = Loggers.get();
  public void setup() throws Exception {
    // do nothing if not overvide
  }

  public abstract void exec() throws Exception;

  public void cleanup() throws Exception {
    // do nothing if not overvide
  }
}
