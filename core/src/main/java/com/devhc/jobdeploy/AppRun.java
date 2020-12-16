package com.devhc.jobdeploy;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AppRun{

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = new SpringApplicationBuilder(
        AppRun.class)
        .web(WebApplicationType.NONE).run(args);
    int exitCode = ctx.getBean(App.class).run(args);
    ctx.stop();
    System.exit(exitCode);
  }
}
