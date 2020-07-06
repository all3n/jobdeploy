package com.devhc.jobdeploy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Created by wanghch on 16/8/26.
 */
@Component
public class DeployCustomConfig {

  private File customConfigDir;
  private File customConfigFile;

  private Properties customProp = null;

  @PostConstruct
  public void init() {
    String currentUserHome = System.getProperty("user.home");
    this.customConfigDir = new File(currentUserHome + File.separator + Constants.CUSTOM_CONFIG_DIR);
    this.customConfigFile = new File(
        customConfigDir + File.separator + Constants.CUSTOM_CONFIG_FILE);
    if (isExists()) {
      InputStreamReader is = null;
      try {
        customProp = new Properties();
        is = new InputStreamReader(new FileInputStream(customConfigFile),
            "UTF-8");
        customProp.load(is);
      } catch (IOException e) {
      } finally {
        IOUtils.closeQuietly(is);
      }

    }
  }

  public String getCustomConfig(String key) {
    if (customProp != null) {
      return customProp.getProperty(key);
    }
    return null;
  }

  public void setCustomConfig(String name, String value) throws IOException {
    if (customProp == null && !isExists()) {
      customConfigDir.mkdirs();
      this.customConfigFile = new File(
          customConfigDir + File.separator + Constants.CUSTOM_CONFIG_FILE);
      customProp = new Properties();
      if(customConfigFile.isFile()) {
        InputStreamReader isr = null;
        isr = new InputStreamReader(new FileInputStream(customConfigFile), "UTF-8");
        customProp.load(isr);
        IOUtils.closeQuietly(isr);
      }
    }
    customProp.put(name, value);
  }

  public boolean isExists() {
    return customConfigDir.exists();
  }

  public void storeToFile() throws IOException {
    if (customProp == null) {
      return;
    }
    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(customConfigFile),
        "UTF-8");
    customProp.store(osw, "custom config of jobdeploy");
    IOUtils.closeQuietly(osw);
  }

}
