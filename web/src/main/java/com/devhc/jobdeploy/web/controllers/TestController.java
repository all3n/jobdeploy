package com.devhc.jobdeploy.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController extends BaseController {

  @GetMapping("/test")
  public String index() {
    return view("index");
  }
}
