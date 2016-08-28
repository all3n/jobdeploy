package com.devhc.jobdeploy.config.structs;

import com.google.common.collect.Maps;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.exception.DeployException;
import org.json.JSONObject;

import java.util.Map;

public class DeployStrategy {
  private Map<String, String> taskStrategyMap = Maps.newHashMap();

  public DeployStrategy(Object obj) {
    if (obj == null) {
      return;
    } else if (obj.getClass() == String.class) {
      taskStrategyMap.put(Constants.TASK_BUILD, String.valueOf(obj));
    } else if (obj.getClass() == JSONObject.class) {
      JSONObject jsonObj = (JSONObject) obj;
      for (Object key : jsonObj.keySet()) {
        String val = jsonObj.getString((String) key);
        taskStrategyMap.put(String.valueOf(key), val);
      }
    } else {
      throw new DeployException("unsupport deploy strategy field type");
    }
  }

  public String get(String task) {
    return taskStrategyMap.get(task);
  }
}
