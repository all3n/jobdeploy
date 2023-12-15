package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.DeployPlugin;
import com.devhc.jobdeploy.exception.DeployException;
import groovy.lang.GroovyClassLoader;
import java.io.File;
import org.slf4j.Logger;

public class GroovyManager {
  private static Logger LOG = Loggers.get();

  public static <T> T loadScript(String name, Class<T> clazz, T defValue) {
    try {
      File local = new File(".");
      String projScript = local.getCanonicalFile() + name;
      File scriptFile = new File(projScript);
      if (!scriptFile.exists()) {
        return defValue;
      }
      LOG.info("load plugin script:{}", projScript);

      GroovyClassLoader gcl = new GroovyClassLoader(GroovyManager.class.getClassLoader());
      gcl.addClasspath(local.getCanonicalPath());
      Class<T> scriptClazz = gcl.parseClass(scriptFile);
      LOG.info("scriptClazz: {}", scriptClazz);
      T jt = (T)scriptClazz.newInstance();
      return jt;
    } catch (Exception e) {
      throw new DeployException(e);
    }
  }
}
