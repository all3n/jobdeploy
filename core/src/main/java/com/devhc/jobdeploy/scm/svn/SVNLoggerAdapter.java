package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.utils.Loggers;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;

public class SVNLoggerAdapter extends SVNDebugLogAdapter {

  private Logger log = Loggers.get();

  public void log(SVNLogType logType, Throwable th, Level logLevel) {
    log.info(logLevel.getName() + getMessage(logType, th.getMessage()), th);
  }

  public void log(SVNLogType logType, String message, Level logLevel) {
    log.info(logLevel.getName() + getMessage(logType, message));
  }

  public void log(SVNLogType logType, String message, byte[] data) {

    try {
      log.info(Level.FINEST.getName() + message + "\n" + new String(data, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      log.info(Level.FINEST.getName() + message + "\n" + new String(data));
    }
  }

  public InputStream createLogStream(SVNLogType logType, InputStream is) {
    return super.createLogStream(logType, is);
  }

  public OutputStream createLogStream(SVNLogType logType, OutputStream os) {
    return super.createLogStream(logType, os);
  }

  private String getMessage(SVNLogType logType, String originalMessage) {
    return logType.getShortName() + ": " + originalMessage;
  }

}
