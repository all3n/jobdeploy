package com.devhc.jobdeploy;

import com.devhc.jobdeploy.args.AppArgs;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.args.ArgsParserHelper;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmDriverFactory;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
 * 部署程序入口
 * @author wanghch
 */
@Component
public class App {
  private static Logger log = LoggerFactory.getLogger(App.class);
  public static String CMD_LINE_SYNTAX = Constants.DEPLOY_SCRIPT_NAME
    + " [headOptions] [stage]:task [taskOptions]";

  @Autowired
  DeployContext deployContext;

  @Autowired
  ConfigurableApplicationContext context;
  private Map<String, Object> tasks;
  @Autowired
  public DeployConfig config;
  @Autowired
  DeployJson deployJson;

  @Autowired
  ScmDriverFactory scmDriverFactory;

  @Autowired
  StrategyManager sm;
  private AppArgs appArgs;
  private CmdLineParser taskOptionParser;
  private CmdLineParser headOptionParser;

  @PostConstruct
  private void init() {
    AnsiColorBuilder.install();
  }

  @PreDestroy
  private void shutdown() {
    AnsiColorBuilder.uninstall();
  }

  public static void main(String[] args) {
    createApp().run(args);
  }

  public static App createApp() {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
      Constants.DEPLOY_CONTEXT_FILE);
    context.registerShutdownHook();
    App app = context.getBean(App.class);
    return app;
  }

  public void run(AppArgs appArgs, String json) throws Exception {
    deployContext.setExecMode(ExecMode.BACKGROUND);
    deployContext.setAppArgs(appArgs);
    this.tasks = context.getBeansWithAnnotation(DeployTask.class);
    JobTask task = getTask(appArgs.getTask());
    deployJson.loadProjectConfigFromJsonString(json);
    initDeployContext();
    runTask(appArgs.getTask());
    if (deployContext.isTmpDirCreate()) {
      deployJson.getDeployServers().cleanDeployTmpDir();
    }
  }

  public void run(String[] args) {
    System.setProperty("java.class.path",
      System.getProperty("java.class.path") + ":" + FileUtils.getExecDir() + "/tasks/*");
    this.appArgs = ArgsParserHelper.parseAppArgs(args);
    deployContext.setAppArgs(appArgs);
    this.tasks = context.getBeansWithAnnotation(DeployTask.class);
    this.headOptionParser = new CmdLineParser(this.deployContext);
    try {
      JobTask task = getTask(appArgs.getTask());
      taskOptionParser = new CmdLineParser(task);
      headOptionParser.parseArgument(appArgs.getHeadOptions());
      taskOptionParser.parseArgument(appArgs.getTaskOptions());
      if (processSpecialCmd()) {
        return;
      }
      loadConfigAndInit();
      initDeployContext();
      log.info("stage:{} task:{}", AnsiColorBuilder.yellow(appArgs.getStage()),
        AnsiColorBuilder.cyan(appArgs.getTask()));
      log.info("{}", AnsiColorBuilder.green(deployJson.toString()));

      if (!deployContext.yes) {
        log.info(AnsiColorBuilder.cyan("are you ready to exec deploy task:{}?   y/n  [default:y]"), appArgs.getTask());
        Scanner scanner = new Scanner(System.in);
        String sure = scanner.nextLine().trim().toLowerCase();
        if ("".equals(sure) || "y".equals(sure) || "yes".equals(sure)) {
        } else {
          log.info("cancel deploy");
          return;
        }
      }

      runTask(appArgs.getTask());
      if (deployContext.isTmpDirCreate()) {
        deployJson.getDeployServers().cleanDeployTmpDir();
      }
    } catch (DeployException e) {
      if (deployContext.verbose) {
        e.printStackTrace();
      }
      log.error(AnsiColorBuilder.red(e.getMessage()));
    } catch (CmdLineException e) {
      printAppUsage();
      return;
    } catch (Exception e) {
      if (deployContext.verbose) {
        e.printStackTrace();
      }
      log.error(AnsiColorBuilder.red(e.getMessage()));
      printAppUsage();
      return;
    }
  }

  private boolean processSpecialCmd() {
    if (deployContext.help) {
      printAppUsage();
      return true;
    } else if (deployContext.list) {
      for (String taskName : getTasks().keySet()) {
        if (taskName.endsWith(Constants.TASK_CLASS_SUFFIX)) {
          taskName = taskName.substring(0, taskName.indexOf(Constants.TASK_CLASS_SUFFIX));
          System.out.println("\t" + AnsiColorBuilder.green(taskName));
        }
      }
      return true;
    } else if (deployContext.version) {
      log.info("deploy version:{}", AnsiColorBuilder.green(Constants.DEPLOY_VERSION));
      return true;
    }
    return false;
  }

  /**
   * init deploy context value
   * @throws CmdLineException
   */
  private void initDeployContext() {
    deployContext.setDeployTimestamp(System.currentTimeMillis());
    deployContext.setDeployid(DeployUtils.getDateTimeStr(deployContext.getDeployTimestamp()));
    // if repository url args is not set ,use deploy.json repository url config
    if (StringUtils.isEmpty(deployContext.getRepositoryUrl()) && StringUtils.isNotEmpty(deployJson.getRepository())) {
      deployContext.setRepositoryUrl(deployJson.getRepository());
    }
    deployContext.setScmDriver(scmDriverFactory.create(deployJson.getScmType()));
  }

  public void loadConfigAndInit() throws IOException {
    deployJson.loadProjectConfig(deployContext.getAppArgs().getStage());
  }

  public JobTask getTask(String name) {
    String taskName = StringUtils.uncapitalize(name);
    String taskFullName = taskName + Constants.TASK_CLASS_SUFFIX;
    JobTask task = (JobTask) getTasks().get(taskFullName);
    return task;
  }

  public void runTask(String taskName) throws Exception {
    String taskStrategy = deployJson.getStrategy().getName();
    ITaskStrategy ts = sm.get(taskName, taskStrategy);
    JobTask jt = getTask(taskName);
    if (jt == null) {
      throw new DeployException("task " + taskName + " not exist");
    }
    jt.setup();
    if (ts == null) {
      log.info(taskName + " start ");
      jt.exec();
    } else {
      log.info(taskName + " use strategy:" + taskStrategy + " start ");
      ts.run(this);
    }
    jt.cleanup();
  }

  public Map<String, Object> getTasks() {
    return tasks;
  }

  public void setTasks(Map<String, Object> tasks) {
    this.tasks = tasks;
  }

  public DeployConfig getConfig() {
    return config;
  }

  public void setConfig(DeployConfig config) {
    this.config = config;
  }

  public DeployJson getDeployJson() {
    return deployJson;
  }

  public void setDeployJson(DeployJson deployJson) {
    this.deployJson = deployJson;
  }

  public <T> T getBean(Class<T> requiredType) {
    return context.getBean(requiredType);
  }

  public DeployContext getDeployContext() {
    return deployContext;
  }

  public void setDeployContext(DeployContext deployContext) {
    this.deployContext = deployContext;
  }

  public CmdLineParser getHeadOptionParser() {
    return headOptionParser;
  }

  public void setHeadOptionParser(CmdLineParser headOptionParser) {
    this.headOptionParser = headOptionParser;
  }

  public CmdLineParser getTaskOptionParser() {
    return taskOptionParser;
  }

  public void setTaskOptionParser(CmdLineParser taskOptionParser) {
    this.taskOptionParser = taskOptionParser;
  }

  public AppArgs getAppArgs() {
    return appArgs;
  }

  private void printAppUsage() {
    log.info(AnsiColorBuilder.magenta(CMD_LINE_SYNTAX));
    log.info(AnsiColorBuilder.cyan("HeaderOptions:"));
    headOptionParser.printUsage(System.out);
    log.info(AnsiColorBuilder.red("TaskOptions:"));
    taskOptionParser.printUsage(System.out);
  }

}
