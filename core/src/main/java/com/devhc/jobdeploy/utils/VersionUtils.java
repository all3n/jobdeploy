package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.exception.DeployException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {
  private static final Pattern vp = Pattern.compile("([<>~=]+)([\\d_]+)");
  public static boolean compareVersion(String versionReq, String version){
    versionReq = versionReq.replace(" ", "");
    Matcher vm = vp.matcher(versionReq);
    String ruleMatch = null;
    String versionMatch = null;
    // default is ok
    boolean versionOk = true;
    if(vm.matches()){
      ruleMatch = vm.group(1);
      versionMatch = vm.group(2);
      String [] versionInfos = version.split("_");
      if(versionInfos.length > 1){
        String deployVersion = versionInfos[0] + "_" + versionInfos[1];
        int verRes = deployVersion.compareTo(versionMatch);
        if(">".equals(ruleMatch) && verRes <= 0) {
          versionOk = false;
        } else if(">=".equals(ruleMatch) && verRes < 0){
          versionOk = false;
        }else if("<".equals(ruleMatch) && verRes >= 0){
          versionOk = false;
        }else if("<=".equals(ruleMatch) && verRes > 0){
          versionOk = false;
        }else if("=".equals(ruleMatch) && verRes != 0){
          versionOk = false;
        }else if("~=".equals(ruleMatch) && verRes == 0){
          versionOk = false;
        }
      }
    }
    return versionOk;
  }
}
