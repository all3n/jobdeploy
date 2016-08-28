package com.devhc.jobdeploy.config.structs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DeployHook {
  private Map<String, DeployHookItem> hookRule;

  public DeployHook(JSONObject hook) {
    hookRule = new HashMap<String, DeployHookItem>();
    Iterator<String> keyIter = hook.keys();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      JSONObject obj = (JSONObject) hook.get(key);
      DeployHookItem dhi = new DeployHookItem(obj);
      hookRule.put(key, dhi);
    }
  }

  public DeployHookItem getHook(String name) {
    return hookRule.get(name);
  }

  public Map<String, DeployHookItem> getHookRule() {
    return hookRule;
  }

  public void setHookRule(Map<String, DeployHookItem> hookRule) {
    this.hookRule = hookRule;
  }

  public class DeployHookItem {
    public static final int BEFORE = 1;
    public static final int AFTER = 2;
    private List<String> before;
    private List<String> after;

    public DeployHookItem(JSONObject obj) {
      if (obj.has("before")) {
        JSONArray beforeArr = obj.getJSONArray("before");
        if (beforeArr.length() > 0) {
          before = new ArrayList<String>();
          for (int i = 0, n = beforeArr.length(); i < n; i++) {
            before.add(beforeArr.getString(i));
          }
        }
      }
      if (obj.has("after")) {
        JSONArray afterArr = obj.getJSONArray("after");
        if (afterArr.length() > 0) {
          after = new ArrayList<String>();
          for (int i = 0, n = afterArr.length(); i < n; i++) {
            after.add(afterArr.getString(i));
          }
        }
      }
    }

    public List<String> getBefore() {
      return before;
    }

    public void setBefore(List<String> before) {
      this.before = before;
    }

    public List<String> getAfter() {
      return after;
    }

    public void setAfter(List<String> after) {
      this.after = after;
    }

  }
}
