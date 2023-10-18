package com.devhc.jobdeploy;

import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.args.AppArgs;
import com.devhc.jobdeploy.args.ArgsParserHelper;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployExtension;
import com.devhc.jobdeploy.event.DeployAppLifeCycle;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.extensions.ExtensionInvocationHandler;
import com.devhc.jobdeploy.extensions.IExtension;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.scm.ScmDriverFactory;
import com.devhc.jobdeploy.strategy.ITaskStrategy;
import com.devhc.jobdeploy.strategy.Strategy;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Scanner;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.apache.log4j.MDC;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 部署程序入口
 *
 * @author wanghch
 */
@Component
public class App extends DeployAppLifeCycle {

    private static Logger log = Loggers.get();
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

    private Map<String, IExtension> extensionMap = Maps.newHashMap();

    @PostConstruct
    private void init() {
        Loggers.init(this);
        AnsiColorBuilder.install();
    }

    @PreDestroy
    private void shutdown() {
        AnsiColorBuilder.uninstall();
    }

    public static void main(String[] args) {
        System.exit(createApp().run(args));
    }

    public static App createApp() {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                Constants.DEPLOY_CONTEXT_FILE);
        context.registerShutdownHook();
        return context.getBean(App.class);
    }


    public void init(AppArgs appArgs, String json) throws Exception {
        this.appArgs = appArgs;
//    MDC.put("task_id", appArgs.getTaskId());
        AnsiColorBuilder.setEnable(false);
        deployContext.setExecMode(ExecMode.BACKGROUND);
        deployContext.setAppArgs(appArgs);
        this.tasks = context.getBeansWithAnnotation(DeployTask.class);
        deployJson.loadProjectConfigFromJsonString(json);
        initDeployContext();
    }

    public int run() throws Exception {
        int exit = 0;
        try {
            appStart();
            JobTask task = getTask(appArgs.getTask());
            this.headOptionParser = new CmdLineParser(this.deployContext);
            taskOptionParser = new CmdLineParser(task);

            headOptionParser.parseArgument(appArgs.getHeadOptions());
            taskOptionParser.parseArgument(appArgs.getTaskOptions());

            runTask(appArgs.getTask());
            if (deployContext.isTmpDirCreate()) {
                deployJson.getDeployServers().cleanDeployTmpDir();
            }
            appSuccess();
        } catch (Exception e) {
            log.error("deploy exception:{}", ExceptionUtils.getStackTrace(e));
            exceptionOccur(e);
            exit = -1;
        } finally {
            appEnd();
        }
        return exit;
    }

    public int run(String[] args) {

//    MDC.put("task_id", "0");
        System.setProperty("java.class.path",
                System.getProperty("java.class.path") + ":" + FileUtils.getExecDir() + "/tasks/*");
        this.appArgs = ArgsParserHelper.parseAppArgs(args);
        deployContext.setAppArgs(appArgs);
        this.tasks = context.getBeansWithAnnotation(DeployTask.class);
        this.headOptionParser = new CmdLineParser(this.deployContext);
        int exitValue = 0;
        try {
            JobTask task = getTask(appArgs.getTask());
            taskOptionParser = new CmdLineParser(task);
            headOptionParser.parseArgument(appArgs.getHeadOptions());
            taskOptionParser.parseArgument(appArgs.getTaskOptions());
            if (processSpecialCmd()) {
                return exitValue;
            }
            loadConfigAndInit();
            initDeployContext();
            loadExtensions();
            log.info("stage:{} task:{}", AnsiColorBuilder.yellow(appArgs.getStage()),
                    AnsiColorBuilder.cyan(appArgs.getTask()));
            if (deployJson.isInit()) {
                log.info("\n{}", AnsiColorBuilder.green(deployJson.toString(4)));
            }

            if (!deployContext.yes) {
                log.info(AnsiColorBuilder
                                .cyan("are you ready to exec deploy task:{}?   y/n  [default:y]"),
                        appArgs.getTask());
                Scanner scanner = new Scanner(System.in);
                String sure = scanner.nextLine().trim().toLowerCase();
                if ("".equals(sure) || "y".equals(sure) || "yes".equals(sure)) {
                } else {
                    log.info("cancel deploy");
                    return exitValue;
                }
            }
            appStart();
            runTask(appArgs.getTask());
            if (deployContext.isTmpDirCreate()) {
                deployJson.getDeployServers().cleanDeployTmpDir();
            }
            appSuccess();
        } catch (DeployException e) {
            if (deployContext.verbose) {
                e.printStackTrace();
            }
            exitValue = -1;
            log.error(AnsiColorBuilder.red(e.getMessage()));
        } catch (CmdLineException e) {
            exitValue = -2;
            printAppUsage();
        } catch (Exception e) {
            exitValue = -3;
            if (deployContext.verbose) {
                e.printStackTrace();
            }
            log.error(AnsiColorBuilder.red(e.getMessage()));
            printAppUsage();
        } finally {
            deployJson.shutdown();
            appEnd();
        }
        return exitValue;
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
     */
    private void initDeployContext() {
        deployContext.setDeployTimestamp(System.currentTimeMillis());
        deployContext.setDeployid(DeployUtils.getDateTimeStr(deployContext.getDeployTimestamp()));

        if (deployJson.isInit()) {
            if (deployJson.getDeployMode() == DeployMode.LOCAL) {
                deployJson.put("scm_type", "dumb");
            }
            deployContext.setScmDriver(scmDriverFactory.create(deployJson.getScmType()));
        }
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
        ITaskStrategy ts = null;
        String taskStrategyName = "";
        if (deployJson.isInit()) {
            Strategy taskStrategy = deployJson.getStrategy();
            if (taskStrategy != null) {
                taskStrategyName = taskStrategy.getName();
                ts = sm.get(taskName, taskStrategyName);
            }
        }
        IExtension ext = extensionMap.get(taskName);
        JobTask jt = getTask(taskName);
        if (jt == null && ext == null) {
            throw new RuntimeException(taskName + " task/ext not exists");
        }
        if (jt != null) {
            jt.setup();
            taskStart(jt);
        }
        if (ext != null) {
            ext.beforeTask();
        }
        if (ext != null && ext.hasMethod("runTask")) {
            ext.runTask();
        } else {
            if (ts != null) {
                log.info(taskName + " use strategy:" + taskStrategyName + " start ");
                ts.run(this);
            } else if (jt != null) {
                log.info(taskName + " start ");
                jt.exec();
            }
        }
        if (ext != null) {
            ext.afterTask();
        }
        if (jt != null) {
            taskEnd(jt);
            jt.cleanup();
        }
    }

    private void loadExtensions() {
        Map<String, DeployExtension> exts = deployJson.getExtensions();
        if (exts == null) {
            return;
        }
        for (Map.Entry<String, DeployExtension> e : exts.entrySet()) {
            log.info("load ext:{}", e);
            DeployExtension ext = e.getValue();
            ext.setup();
            try {
                IExtension pExt = (IExtension) Proxy.newProxyInstance(IExtension.class.getClassLoader(),
                        new Class[]{IExtension.class}, new ExtensionInvocationHandler(ext, context, deployContext));
                extensionMap.put(e.getKey(), pExt);
            } catch (Throwable ex) {
                ex.printStackTrace();
                log.error("load {} extension fail:{}", ext.getName(), ex.getMessage());
            }
        }
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
