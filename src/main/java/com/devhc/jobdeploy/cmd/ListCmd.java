package com.devhc.jobdeploy.cmd;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.annotation.DeployCmd;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DeployCmd(value = "l", description = "list all task", longOpt = "list")
public class ListCmd implements JobCmd {

  private static Logger log = LoggerFactory.getLogger("List");
  @Autowired
  App app;

  @Override
  public void run() {
    System.out.println(AnsiColorBuilder.green("--list all task--"));
    for (String taskName : app.getTasks().keySet()) {
      if (taskName.endsWith(Constants.TASK_CLASS_SUFFIX)) {
        taskName = taskName.substring(0, taskName.indexOf(Constants.TASK_CLASS_SUFFIX));
        System.out.println("\t" + AnsiColorBuilder.green(taskName));
      }
    }

  }

}
