package com.devhc.jobdeploy.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.webjars.RequireJS;

@Controller
public class IndexController extends BaseController {

  @Autowired
  public Environment env;

  @GetMapping("/")
  public String index() {
    return "default";
  }


  @ResponseBody
  @RequestMapping(value = "/webjarsjs", produces = "application/javascript")
  public String webjarjs() {
    return RequireJS.getSetupJavaScript("/webjars/");
  }


  @ResponseBody
  @RequestMapping(value = "/configjs", produces = "application/javascript")
  public String configjs() {
    JSONObject json = new JSONObject();
    try {
      json.put("rest.api.base", env.getProperty("spring.data.rest.base-path"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return "window.APP_CONFIG="+json.toString()+";";
  }
}
