package com.devhc.jobdeploy.scm.svn;

import com.devhc.jobdeploy.tasks.ScmTask;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import org.slf4j.Logger;

public class SvnHelper {
  private static Logger log = Loggers.get(ScmTask.class);

  public static void checkout(String repository, String username,
    String password, String target) {
    String cmd = "svn checkout --username " + username + " --password "
      + password + " -r HEAD --non-interactive --trust-server-cert "
      + repository + " " + target;
    CmdHelper.execCmd(cmd, ".", log);
  }

  public static void update(String target, String username, String password) {
    String cmd = "svn update";
    CmdHelper.execCmd(cmd, ".", log);
  }
}
