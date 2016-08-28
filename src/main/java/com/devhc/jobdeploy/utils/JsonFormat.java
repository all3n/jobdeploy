package com.devhc.jobdeploy.utils;

import java.util.Stack;

public class JsonFormat {

  private static Stack<Integer> stack = new Stack<Integer>();

  public static String format(String json, int depth) {
    String result = "";

    for (int i = 0; i < json.length(); i++) {
      char c = json.charAt(i);
      if (c == '[')
        stack.push(i);
      if (c == ']') {
        int popTag = stack.pop();
        if (stack.isEmpty()) {
          String childJson = json.substring(popTag + 1, i);
          // 递归解析数组
          result += "[\n\t" + format(childJson, depth + 2)
            + tokenStr(depth) + "";
        }
      }
      if (stack.isEmpty()) {
        String token = getToken(c, depth, json, i);
        result += token;
      }
    }
    return result;

  }

  private static String getToken(char c, int depth, String json, int i) {
    if (c == '{' || c == '}')
      return "\n" + tokenStr(depth + 1) + c + "\n" + tokenStr(depth + 2);
    if (c == ',' && json.charAt(i + 1) == '\"')
      return c + "\n" + tokenStr(depth + 2);
    if (c == ']')
      return "\n" + tokenStr(depth + 2) + c + "\n" + tokenStr(depth + 2);
    return c + "";
  }

  // 分隔符
  private static String tokenStr(int depth) {
    String token = "";
    for (int i = 0; i < depth; i++)
      token += "   ";
    return token;
  }
}
