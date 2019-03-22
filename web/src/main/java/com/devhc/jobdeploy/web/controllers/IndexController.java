package com.devhc.jobdeploy.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.webjars.RequireJS;

@Controller
public class IndexController extends BaseController {

  @Autowired
  public Environment env;

  @GetMapping({"/", "/login"})
  public String index() {
    return "login";
  }

  @PostMapping("/user/login")
  public String login(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    if ("admin".equals(username) && "123456".equals(password)) {
      return "dashboard";
    } else {
      return "login";
    }
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
    return "window.APP_CONFIG=" + json.toString() + ";";
  }
}
