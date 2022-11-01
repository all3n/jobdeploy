package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class DeployUtils {

  private static Pattern VAR_PATTERN = Pattern.compile("\\$\\{([\\w-]+)\\}");
  private static Logger log = Loggers.get();

  public static String formatColonStr(String param) {
    if (param == null || "".equals(param.trim())) {
      return "";
    }
    int len = param.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = param.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i != 0) {
          sb.append(':');
        }
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static String getDateTimeStr(long timestamp) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    return sdf.format(new Date(timestamp));
  }

  public static Set<String> parseVars(String rawStr) {
    Matcher m = VAR_PATTERN.matcher(rawStr);
    Set<String> ret = Sets.newHashSet();
    while (m.find()) {
      ret.add(m.group(1));
    }
    return ret;
  }


  public static String parseRealValue(String rawStr, Object obj, String defaultValue) {
    String ret = parseRealValue(rawStr, obj);
    if (StringUtils.isEmpty(ret)) {
      return defaultValue;
    } else {
      return ret;
    }
  }

  public static String parseRealValue(String rawStr, Object obj) {
    String ret = rawStr;
    Set<String> vars = parseVars(ret);
    for (String v : vars) {
      String getMethodName = "get" + StringUtils.capitalize(v);
      Method method;
      try {
        method = obj.getClass().getMethod(getMethodName);
        ret = ret.replace("${" + v + "}", method.invoke(obj).toString());
      } catch (NoSuchMethodException e) {
        if(obj instanceof DeployJson){
          DeployJson jobj = (DeployJson) obj;
          if(jobj.has(v)){
            String vRes = jobj.getProperty(v, v);
            ret = ret.replace("${" + v + "}", vRes);
          }
        }
        log.warn(AnsiColorBuilder.red(v + " is not exist,please check config property  "));
      } catch (Exception e) {
        throw new DeployException(e);
      }
    }
    return ret;
  }

  public static String addPrefixIfPathIsRelative(String path, String prefix) {
    if (path.startsWith("/")) {
      return path;
    } else {
      return prefix + path;
    }
  }

  public static String removeFirstSlash(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    } else {
      return path;
    }
  }


  public static void main(String[] args) {
    try {
      System.out.println(parseRealValue("asd${b}f_${aa}_afs", null));
    } catch (Exception e) {
      e.printStackTrace();

    }
  }
}
