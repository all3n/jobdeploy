package com.devhc.jobdeploy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppRun implements CommandLineRunner {

  @Autowired
  App app;

  public static void main(String[] args) {
    SpringApplication.run(AppRun.class, args);
  }

  @Override
  public void run(String... args) {
    app.run(args);
  }
}
