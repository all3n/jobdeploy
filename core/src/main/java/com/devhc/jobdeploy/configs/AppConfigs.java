package com.devhc.jobdeploy.configs;

import com.devhc.jobdeploy.FlowManager;
import com.devhc.jobdeploy.config.AppConfig;
import com.devhc.jobdeploy.config.DeployJson;
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
  @Autowired
  DeployJson dj;

  @Bean
  public FlowManager flowManager(){
    List<String> flows = dj.getFlows();
    if(flows != null && flows.size() > 0){
      return new FlowManager(flows);
    }else{
      return new FlowManager(config.getFlows());
    }
  }

//  @Bean
//  public JavaMailSender mailSender(){
//    JavaMailSenderImpl mail = new JavaMailSenderImpl();
//    mail.setJavaMailProperties();
//
//    return mail;
//  }
}
