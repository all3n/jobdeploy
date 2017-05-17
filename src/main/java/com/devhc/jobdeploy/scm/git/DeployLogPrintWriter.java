package com.devhc.jobdeploy.scm.git;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.event.DeployAppLogEvent;
import com.devhc.jobdeploy.tasks.ScmTask;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Created by wanghch on 16/11/22.
 */
public class DeployLogPrintWriter extends PrintWriter {
  private App app;

  public DeployLogPrintWriter(App app) {
    super(System.err);
    this.app = app;
  }

  @Override public void write(String s) {
    super.write(s);
    app.log(s, DeployAppLogEvent.INFO, ScmTask.class.getSimpleName());
  }
}
