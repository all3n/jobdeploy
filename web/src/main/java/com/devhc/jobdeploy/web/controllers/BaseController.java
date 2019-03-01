package com.devhc.jobdeploy.web.controllers;

public class BaseController {

  public String view(String name) {
    String base = getClass().getSimpleName().replace("Controller", "").toLowerCase();
    return base + "/" + name;
  }
}
