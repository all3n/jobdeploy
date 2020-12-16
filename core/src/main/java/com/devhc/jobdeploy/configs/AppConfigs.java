package com.devhc.jobdeploy.configs;

import com.devhc.jobdeploy.FlowManager;
import com.devhc.jobdeploy.config.AppConfig;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class AppConfigs {
  @Autowired
  AppConfig config;

  @Bean
  public FlowManager flowManager(){
    return new FlowManager(config.getFlows());
  }

//  @Bean
//  public JavaMailSender mailSender(){
//    JavaMailSenderImpl mail = new JavaMailSenderImpl();
//    mail.setJavaMailProperties();
//
//    return mail;
//  }
}
