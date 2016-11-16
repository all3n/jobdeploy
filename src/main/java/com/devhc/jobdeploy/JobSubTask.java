package com.devhc.jobdeploy;

import com.devhc.jobdeploy.annotation.TaskSubCmd;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * parent class of task has sub cmd
 * subCmdClass cmd should has Annotation @TaskSubCmd
 *
 * @DeployTask
 * public class SubCmdTask extends JobSubTask {
 *    @TaskSubCmd(value = "xxx",desc = "xxx")
 *    public void xxxx() {
 *      System.out.println("xx");
 *    }
 * }
 *
 * @author wanghch
 * Created by wanghch on 16/11/15.
 */
@Component
public class JobSubTask extends JobTask {
  @Autowired App app;
  private static Logger logger = Loggers.get();
  private Map<String, Method> subCmdMap = Maps.newHashMap();
  private Map<String, TaskSubCmd> taskSubAnoMap = Maps.newHashMap();

  @PostConstruct
  private void init() {
    // init search method has annotation @TaskSubCmd
    Method[] methods = getClass().getMethods();
    for (Method method : methods) {
      TaskSubCmd taskSubCmdAno = method.getAnnotation(TaskSubCmd.class);
      if (taskSubCmdAno != null) {
        if (StringUtils.isNotEmpty(taskSubCmdAno.value())) {
          subCmdMap.put(taskSubCmdAno.value(), method);
          taskSubAnoMap.put(taskSubCmdAno.value(), taskSubCmdAno);
        } else {
          String lowerMethodName = method.getName().toLowerCase();
          subCmdMap.put(lowerMethodName, method);
          taskSubAnoMap.put(lowerMethodName, taskSubCmdAno);
        }
      }
    }
  }

  @Override public void exec() throws Exception {
    List<String> subCmds = app.getAppArgs().getSubCmds();
    if (subCmds.size() == 0) {
      printHelp();
      return;
    }
    // invoke subCmd use reflection
    String subCmd = subCmds.get(0);
    Method method = subCmdMap.get(subCmd);
    if (method == null) {
      logger.error("{} subCmd invalid", subCmd);
      printHelp();
      return;
    }
    method.invoke(this);
  }

  private void printHelp() {
    logger.info("available subcmd:");
    for (String key : subCmdMap.keySet()) {
      logger.info("\t\t{} \t\t {}", key, taskSubAnoMap.get(key).desc());
    }
  }
}
