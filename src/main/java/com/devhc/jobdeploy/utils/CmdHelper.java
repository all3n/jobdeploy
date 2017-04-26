package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.log.DeployAppLogger;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class CmdHelper {
  private static DeployAppLogger log = (DeployAppLogger) Loggers.get();

  // TODO common exec cannot exec mvn
  public static boolean execCmd2(String cmd, String dir) {
    log.info(cmd);
    CommandLine cmdLine = CommandLine.parse(cmd);
    DefaultExecutor executor = new DefaultExecutor();
    int exitValue = 0;
    try {
      executor.setWatchdog(new ExecuteWatchdog(60000));
      exitValue = executor.execute(cmdLine);
    } catch (Exception e) {
      exitValue = 1;
      e.printStackTrace();
    }
    log.info(cmd + " exitValue:" + exitValue);

    return exitValue == 0;
  }

  public static void execCmd(String cmd, String dir) {
    log.info("[{}]:{}", AnsiColorBuilder.green(dir), AnsiColorBuilder.yellow(cmd));
    Runtime run = Runtime.getRuntime();
    BufferedInputStream inError = null;
    BufferedReader inBrError = null;
    File execPath = new File(dir);
    BufferedInputStream in = null;
    BufferedReader inBr = null;
    try {
      Process p = run.exec(cmd, null, execPath);
      in = new BufferedInputStream(p.getInputStream());
      inBr = new BufferedReader(new InputStreamReader(in));
      String lineStr;
      while ((lineStr = inBr.readLine()) != null) {
        log.sysout(lineStr);
      }
      if (p.waitFor() != 0) {
        if (p.exitValue() == 1) {
          inError = new BufferedInputStream(p.getErrorStream());
          inBrError = new BufferedReader(new InputStreamReader(inError));
          throw new DeployException(AnsiColorBuilder.red(cmd + " run failed :" + inBrError.readLine()));
        }

      }
    } catch (Exception e) {
      throw new DeployException(e);
    } finally {
      IOUtils.closeQuietly(inBr);
      IOUtils.closeQuietly(in);

      IOUtils.closeQuietly(inBrError);
      IOUtils.closeQuietly(inError);
    }
  }

  public static void main(String args[]) {
    CmdHelper.execCmd2("mvn test", ".");
  }
}
