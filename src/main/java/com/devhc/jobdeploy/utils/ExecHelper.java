package com.devhc.jobdeploy.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

import java.io.IOException;

public class ExecHelper {

  public static void main(String[] args) throws ExecuteException, IOException {
    String line = "ping www.baidu.com";
    CommandLine cmdLine = CommandLine.parse(line);
    DefaultExecutor executor = new DefaultExecutor();
    int exitValue = executor.execute(cmdLine);
  }

}
