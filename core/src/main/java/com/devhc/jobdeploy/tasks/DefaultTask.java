package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.FlowManager;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.ScriptTask;
import com.devhc.jobdeploy.config.structs.DeployHook;
import com.devhc.jobdeploy.config.structs.DeployHook.DeployHookItem;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.utils.Loggers;
import java.util.List;
import java.util.Map;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * default task: execute entire deploy pipeline predefined
 * deploy test = deploy test:default
 *
 * @author wanghch
 */
@DeployTask
public class DefaultTask extends JobTask {

    private static Logger log = Loggers.get();

    @Autowired
    DeployJson dc;

    @Autowired
    DeployConfig deployConfig;

    @Autowired
    DeployContext deployContext;

    @Autowired
    FlowManager flowManager;

    @Autowired
    App app;

    @Option(name = "-b", usage = "branch you want to deploy", aliases = "--branch")
    private String branch;

    @Option(name = "-t", usage = "tag you want to deploy", aliases = "--tags")
    private String tag;

    @Option(name = "-uj", usage = "upload job to azkaban", aliases = "--uploadJobs")
    private boolean uploadJobs;

    @Option(name = "-d", usage = "delete if commitid is same", aliases = "--delete")
    private boolean delete = false;

    @Option(name = "-r", usage = "revision", aliases = "--revision")
    private String revision;

    @Override
    public void exec() throws Exception {
        System.out.println("uploadJobs:" + uploadJobs);
        app.getDeployContext().setUploadJob(uploadJobs);
        processFlow();
    }

    /**
     *
     */
    private void processFlow() throws Exception {
        DeployContext ctx = app.getDeployContext();
        final ScmDriver scm = ctx.getScmDriver();
        scm.setBranch(branch);
        scm.setTag(tag);
        scm.setRevision(revision);
        scm.init(dc.getRepository(), ctx.getSrcDir());
        if (delete) {
            log.info("delete same commit old dir:{}", delete);
            dc.getDeployServers().exec(new DeployServerExecCallback() {
                @Override
                public void run(DeployJson dc, DeployServer server) throws Exception {
                    String releaseDir = deployContext.getReleseDir();
                    String deployTo = server.getDeployto();
                    String targetRelease = deployTo + "/" + releaseDir;
                    server.getDriver().execCommand("rm -rf " + targetRelease);
                }
            });
        }
        if(dc.getFlows() != null && dc.getFlows().size() > 0){
            // overwrite default flows
            flowManager.setFlows(dc.getFlows());
        }
        Map<String, ScriptTask> custTasks = dc.getTasks();
        for (String flow : flowManager.getFlows()) {
            log.info(AnsiColorBuilder.cyan("-----------flow " + flow
                + " start------------------------------"));
            log.info(flow + " task start");
            processHook(flow, DeployHookItem.BEFORE);
            if(custTasks != null && custTasks.containsKey(flow)){
                ExecTask.processScriptTask(dc, flow, false);
            }else {
                app.runTask(flow);
            }
            processHook(flow, DeployHookItem.AFTER);
            log.info(AnsiColorBuilder.magenta("-----------flow " + flow
                + " end-------------------------------"));
        }

    }

    /**
     * 处理 hook 操作 BEFORE|AFTER
     */
    private void processHook(String flow, int scenario) throws Exception {
        DeployHook hooks = dc.getHooks();
        if (hooks == null) {
            return;
        }

        DeployHookItem hookItem = hooks.getHook(flow);
        if (hookItem == null) {
            return;
        }
        final List<String> cmds = scenario == DeployHookItem.BEFORE ? hookItem
            .getBefore() : hookItem.getAfter();

        if (cmds == null || cmds.size() == 0) {
            return;
        }
        dc.getDeployServers().exec(new DeployServerExecCallback() {
            public void run(DeployJson config, DeployServer server) {
                for (String cmd : cmds) {
                    // hook support custom task if cmd start with @
                    if (cmd.startsWith("@")) {
                        if (cmd.length() > 1) {
                            String taskName = cmd.substring(1);
                            ScriptTask st = null;
                            if(dc.getTasks() != null) {
                                st = dc.getTasks().get(taskName);
                            }
                            if (st != null) {
                                try {
                                    ExecTask.processScriptTask(dc, taskName, false);
                                } catch (Exception e) {
                                    throw new DeployException(e);
                                }
                            } else {
                                throw new DeployException("task " + taskName + " invalid");
                            }
                        } else {
                            throw new DeployException("hook cmd need taskName");
                        }
                    } else {
                        String deployTo = server.getDeployto();

                        cmd = cmd.replace("${deployto}", deployTo);
                        cmd = cmd.replace("${server}", server.getServer());
                        cmd = DeployUtils.parseRealValue(cmd, dc);
                        String execDir = deployTo + "/" + Constants.REMOTE_CURRENT_DIR;
                        server.getDriver().execCommand(cmd, execDir);
                    }
                }
            }
        });

    }
}
