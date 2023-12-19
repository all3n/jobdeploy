package com.devhc.jobdeploy.config.structs;

import com.devhc.jobdeploy.config.DeployJson;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.utils.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeployPattern {

  List<Pattern> includes = Lists.newArrayList();
  List<Pattern> excludes = Lists.newArrayList();
  JSONObject obj;

  public DeployPattern(JSONObject obj) {
    this.obj = obj;
    if (obj != null) {
      if (obj.has("includes")) {
        JSONArray inArr = obj.getJSONArray("includes");
        for (int i = 0; i < inArr.length(); ++i) {
          includes.add(Pattern.compile(inArr.getString(i)));
        }
      }
      if (obj.has("excludes")) {
        JSONArray exArr = obj.getJSONArray("excludes");
        for (int i = 0; i < exArr.length(); ++i) {
          excludes.add(Pattern.compile(exArr.getString(i)));
        }
      }
    }
  }


  public boolean filter(String path) {
    if (obj == null) {
      return false;
    }
    boolean flag = false;
    if (!includes.isEmpty()) {
      // if include is set default is filter
      // only in include pattern can pass the filter
      flag = true;
      for (Pattern pi : includes) {
        Matcher mi = pi.matcher(path);
        if (mi.find()) {
          flag = false;
          break;
        }
      }
    }
    for (Pattern pe : excludes) {
      Matcher me = pe.matcher(path);
      if (me.find()) {
        flag = true;
        break;
      }
    }
    return flag;
  }
}
