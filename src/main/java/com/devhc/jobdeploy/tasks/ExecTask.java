package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SCPClient;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployPlugin;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.utils.FileUtils;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileFilter;

@DeployTask
public class ExecTask extends JobTask {
  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  private static Logger log = LoggerFactory.getLogger(ExecTask.class);

  @Option(name = "-tf", usage = "task file you want to execute", aliases = "--taskfile")
  private String taskFile;

  @Option(name = "-local", usage = "is execute in local")
  private boolean local;

  public void exec() throws Exception {
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
        CmdHelper.execCmd(execTaskFile, buildDir);
      }
    } else {
      dc.getDeployServers().exec(new DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServer server)
          throws Exception {
          String release = server.getDeployto()
            +"/"+ Constants.REMOTE_TASKS_DIR;
          SSHDriver driver = server.getDriver();

          driver.mkdir(release, server.getChmod(), server.getChown());

          SCPClient scpClient = driver.getScpClient();
          scpClient.put(execTaskFile, release);
          driver.changePermission(release + "/" + taskFile,
            dc.getChmod(), dc.getChown());
          driver.execCommand("cd " + server.getDeployto() + ";"
            + release + "/" + taskFile);
        }
      });
    }
  }
}
