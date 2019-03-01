package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SCPClient;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployPlugin;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.ScriptTask;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class ExecTask extends JobTask {

  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  private static Logger log = Loggers.get();

  @Option(name = "-tf", usage = "task file you want to execute", aliases = "--taskfile")
  private String taskFile;

  @Option(name = "-local", usage = "is execute in local")
  private boolean local;

  public void exec() throws Exception {
    if (app.getAppArgs().getSubCmds().size() > 0) {
      taskFile = app.getAppArgs().getSubCmds().get(0);
    }
    // exec script task
    if (dc.getTasks().containsKey(taskFile)) {
      processScriptTask(dc, taskFile, local);
      return;
    }

    String buildDir = app.getDeployContext().getBuildDir();
    String taskDir = FileUtils.getExecDir() + "/" + dc.getTasksDir();
    File taskDirFile = new File(taskDir);
    if (StringUtils.isEmpty(taskFile)) {
      File taskFiles[] = taskDirFile.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return true;
        }
      });
      log.info(AnsiColorBuilder.yellow("external task available as follow"));
      for (File tf : taskFiles) {
        if (tf.isFile()) {
          log.info("\t{}", AnsiColorBuilder.cyan(tf.getName()));
        }
      }
    }

    final String execTaskFile = taskDir + "/" + taskFile;
    File f = new File(execTaskFile);
    if (!f.exists()) {
      throw new DeployException(execTaskFile + " is not exists");
    } else if (!f.canExecute()) {
      throw new DeployException(execTaskFile
          + " cannot execute,please check file permission");
    }

    if (local) {
      log.info("exec local {}", execTaskFile);
      if (execTaskFile.endsWith(".groovy")) {
        GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader());
        Class<DeployPlugin> Clazz = gcl.parseClass(f);
        DeployPlugin jt = Clazz.newInstance();
        jt.run(app);
      } else {
        CmdHelper.execCmd(execTaskFile, buildDir, log);
      }
    } else {
      dc.getDeployServers().exec(new DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServer server)
            throws Exception {
          String deployTo = server.getDeployto();
          String chmod = server.getChmod();
          String chown = server.getChown();

          String release = deployTo
              + "/" + Constants.REMOTE_TASKS_DIR;
          SSHDriver driver = server.getDriver();

          driver.mkdir(release, chmod, chown);

          SCPClient scpClient = driver.getScpClient();
          scpClient.put(execTaskFile, release);
          driver.changePermission(release + "/" + taskFile,
              chmod, chown);
          driver.execCommand("cd " + deployTo + ";"
              + release + "/" + taskFile);
        }
      });
    }
  }

  public static void processScriptTask(DeployJson dc, String taskName, boolean local)
      throws Exception {
    final ScriptTask st = dc.getTasks().get(taskName);
    if (local) {
      for (final String cmd : st.getCmd()) {
        CmdHelper.execCmd(cmd, st.getDir(), log);
      }
    } else {
      dc.getDeployServers().exec(new DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServer server)
            throws Exception {
          for (final String cmd : st.getCmd()) {
            String execDir = "";
            String deployTo = server.getDeployto();
            if (StringUtils.isEmpty(st.getDir()) || ".".equals(st.getDir())) {
              execDir = deployTo + "/" + Constants.REMOTE_CURRENT_DIR;
            } else if (st.getDir().startsWith("/")) {
              execDir = st.getDir();
            } else {
              execDir = deployTo + "/" + Constants.REMOTE_CURRENT_DIR
                  + "/" + st.getDir();
            }
            SSHDriver driver = server.getDriver();
            driver.execCommand(cmd, execDir);
          }
        }
      });
    }
  }
}
