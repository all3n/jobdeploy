package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class ValidateTask extends JobTask {

  @Autowired
  DeployJson dc;
  @Autowired
  private static Logger log = Loggers.get();
  public static String RequiredField = "servers";

  public void exec() throws Exception {
    String rf[] = StringUtils.split(RequiredField, ",");
    for (String field : rf) {
      try {
        if (dc.get(field) == null) {
          throw new DeployException("required field " + field + " is null");
        }
      } catch (JSONException e) {
        throw new DeployException("required field " + field + " not exists");
      }
    }
  }
}
