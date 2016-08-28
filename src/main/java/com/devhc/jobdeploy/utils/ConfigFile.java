package com.devhc.jobdeploy.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFile extends Properties {
  private static Logger log = LoggerFactory.getLogger(ConfigFile.class);
  private HashMap<String, String> paramsMap = Maps.newHashMap();
  private static Pattern VAR_PATTERN = Pattern.compile("\\{([\\w-.]+)\\}");

  /**
   * 默认取ConfigFile 子类，将驼峰类名 改成 下划线 命名 对应配置文件
   */
  public ConfigFile() {
    init(camelToUnderline(getClass().getSimpleName() + "_default.properties"), false);
    init(camelToUnderline(getClass().getSimpleName() + ".properties"), false);
  }

  private void init(String configName, boolean isRequired) {
    try {
      InputStream is = ConfigFile.class.getClassLoader().getResourceAsStream(
        configName);
      if (is != null) {
        load(is);
      } else {
        if (isRequired) {
          throw new IOException(configName + " is requried");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void load(InputStream resourceAsStream, boolean isRequired) {

  }

  public Integer getInt(String key) {
    Integer value = Integer.parseInt(getProperty(key));
    return value;
  }

  /**
   * 解析 string 包含 {var}
   * @param value
   * @return
   */
  public String getRealValue(String value) {
    Set<String> vars = parseVars(value);
    if (vars.size() > 0) {
      for (String var : vars) {
        if (containsKey(var)) {
          value = value.replace("{" + var + "}",
            getProperty(var));
        } else if (paramsMap.containsKey(var)) {
          value = value.replace("{" + var + "}",
            paramsMap.get(var));
        } else {
          throw new RuntimeException("{" + var + "} not valid");
        }
      }
      vars = parseVars(value);
      if (vars.size() > 0) {
        value = getRealValue(value);
      }
    }
    return value;
  }

  @Override
  public String getProperty(String key) {
    String rawPropValue = super.getProperty(key);
    return getRealValue(rawPropValue);
  }

  public HashMap<String, String> getParamsMap() {
    return paramsMap;
  }

  public void setParamsMap(HashMap<String, String> paramsMap) {
    this.paramsMap = paramsMap;
  }

  public void addParamsValue(String name, String value) {
    this.paramsMap.put(name, value);
  }

  public static Set<String> parseVars(String name) {
    Set<String> varsSet = Sets.newHashSet();
    Matcher matcher = VAR_PATTERN.matcher(name);
    while (matcher.find()) {
      varsSet.add(matcher.group(1));
    }
    return varsSet;
  }

  public static String camelToUnderline(String param) {
    if (param == null || "".equals(param.trim())) {
      return "";
    }
    int len = param.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = param.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i != 0) {
          sb.append('_');
        }
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
