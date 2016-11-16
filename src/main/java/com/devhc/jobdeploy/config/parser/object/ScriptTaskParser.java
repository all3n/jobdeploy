package com.devhc.jobdeploy.config.parser.object;

import com.devhc.jobdeploy.config.ScriptTask;
import com.devhc.jobdeploy.config.parser.JsonObjectParser;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by wanghch on 16/11/11.
 */
public class ScriptTaskParser implements JsonObjectParser<ScriptTask> {
  @Override public ScriptTask parse(JSONObject a) {
    ScriptTask st = new ScriptTask();
    st.setName(a.getString("name"));
    st.setDir(a.optString("dir", "."));
    List<String> cmdList = Lists.newArrayList();
    JSONArray cmd = a.getJSONArray("cmd");
    for (int i = 0; i < cmd.length(); i++) {
      cmdList.add(cmd.getString(i));
    }
    st.setCmd(cmdList);
    return st;
  }
}
