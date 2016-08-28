package com.devhc.jobdeploy.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strategy {
  // name[arg]
  public static final String PATTERN_STRATEGY = "([\\w:]+)(\\[(\\w+)\\])?";
  private String name;
  private String args;
  private static Pattern syntaxPattern = Pattern.compile(PATTERN_STRATEGY);

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArgs() {
    return args;
  }

  public void setArgs(String args) {
    this.args = args;
  }

  public static Strategy parse(String str) {
    Matcher matcher = syntaxPattern.matcher(str);
    if (!matcher.matches()) {
      return null;
    }
    Strategy s = new Strategy();
    s.setName(matcher.group(1));
    s.setArgs(matcher.group(3));

    return s;
  }
}
