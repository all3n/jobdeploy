package com.devhc.jobdeploy.notify;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.utils.HttpClientHelper;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

public class HttpNotify extends BaseNotify {

  @Override
  public void alert(App app, JSONObject alertObj) {
    DeployJson dc = app.getDeployJson();
    String type = alertObj.getString("type");
    if ("http".equals(type)) {
      String url = alertObj.getString("url");
      String method = alertObj.getString("method");
      JSONObject params = alertObj.optJSONObject("params");
      String auth = alertObj.optString("auth");
      JSONObject headers = alertObj.optJSONObject("headers");
      Map<String, String> headerMap = Maps.newTreeMap();
      Iterator ki = headers.keys();
      while (ki.hasNext()) {
        String hk = (String) ki.next();
        headerMap.put(hk, headers.getString(hk));
      }
      HttpClientHelper httpClient = new HttpClientHelper();
      if (StringUtils.isNotEmpty(auth)) {
        httpClient.setAuth(auth);
      }
      httpClient.init();
      List<String> msgs = Lists.newArrayList();
      for (DeployServer ds : dc.getDeployServers().getServers()) {
        if (!ds.getDriver().isValid()) {
          String error = ds.getDriver().getError();
          String msg =
              dc.getName() + ":" + dc.getStage() + ":" + ds.getServer() + " "+ error;
          msgs.add(msg);
        }
      }

      StringBuffer sb = new StringBuffer();
      int n = 0;
      for (int i = 0; i < msgs.size(); i++) {
        n += 1;
        sb.append(msgs.get(i) + "\n");
        if (n % 10 == 0) {
          send(httpClient, sb.toString(), params, method, url, headerMap);
          n = 0;
          sb = new StringBuffer();
        }
      }
      if (n > 0) {
        send(httpClient, sb.toString(), params, method, url, headerMap);
      }
    }
  }


  public void send(HttpClientHelper httpClient, String msg, JSONObject params, String method,
      String url, Map<String, String> headerMap) {
    Map<String, String> paramsMap = new HashMap<>();
    Iterator pi = params.keys();
    while (pi.hasNext()) {
      String pk = (String) pi.next();
      String pval = params.getString(pk).replace("{msg}", msg);
      paramsMap.put(pk, pval);
    }
    if ("post".equals(method)) {
      httpClient.post(url, paramsMap, headerMap);
    } else if ("get".equals(method)) {
      httpClient.get(url, paramsMap, headerMap);
    }
  }
}
