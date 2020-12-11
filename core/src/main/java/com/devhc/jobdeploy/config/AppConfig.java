package com.devhc.jobdeploy.config;

import java.util.List;
import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "jobdeploy")
public class AppConfig {
  private List<String> flows;
//  private Properties mail;

}
