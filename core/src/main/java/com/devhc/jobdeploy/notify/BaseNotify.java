package com.devhc.jobdeploy.notify;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;
import org.json.JSONObject;

public abstract class BaseNotify {
  public abstract void alert(App app, JSONObject alertObj);
}
