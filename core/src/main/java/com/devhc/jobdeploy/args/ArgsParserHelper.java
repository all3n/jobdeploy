package com.devhc.jobdeploy.args;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import com.devhc.jobdeploy.utils.NTuple;
import com.devhc.jobdeploy.utils.ParserUtils;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ArgsParserHelper {

    private static Logger log = Loggers.get();

    /**
     * 解析参数
     */
    public static AppArgs parseAppArgs(String[] args) {
        AppArgs appArgs = new AppArgs();
        if (args.length == 0) {
            return appArgs;
        }

        NTuple<List<String>> argTuple = ParserUtils.getArgment(DeployContext.class, args);
        appArgs.setHeadOptions(argTuple.at(0));
        List<String> leftArgs = argTuple.at(1);
        if (leftArgs.isEmpty()) {
            return appArgs;
        }
        String stageTask = leftArgs.get(0);
        if (leftArgs.size() > 1) {
            appArgs.setTaskOptions(leftArgs.subList(1, leftArgs.size()));
        }
        String stageTaskArr[] = stageTask.split(":", 3);
        switch (stageTaskArr.length) {
            case 1:
                appArgs.setStage(stageTaskArr[0]);
                break;
            case 2:
                appArgs.setStage(stageTaskArr[0]);
                appArgs.setTask(stageTaskArr[1]);
                break;
            case 3:
                appArgs.setStage(stageTaskArr[0]);
                appArgs.setTask(stageTaskArr[1]);
                appArgs.setSubCmd(Arrays.asList(stageTaskArr[2].split(":")));
                break;
        }
        Map<String, String> cusOpts = appArgs.getCustomOptions();
        argTuple.at(2).stream().forEach(t -> {
            String vals[] = t.split("=");
            cusOpts.put(vals[0], vals[1]);
        });

        return appArgs;
    }

    public static AppArgs parseAgs(String[] args) {
        AppArgs appArgs = new AppArgs();
        List<String> headCmdOptions = Lists.newArrayList();
        String stage = "";
        if (args.length > 0 && !args[0].startsWith("-")) {
            stage = args[0];
            for (int i = 0; i < args.length; i++) {
                String v = args[i];
                if (!v.startsWith("-")) {
                    headCmdOptions.add(v);
                } else {
                    break;
                }
            }

            List<String> argsList = Arrays.asList(args);
            argsList = argsList.subList(headCmdOptions.size(), argsList.size());
            appArgs.setTaskOptions(argsList);
            args = argsList.toArray(new String[argsList.size()]);
        }

        appArgs.setStage(stage);
        String task = Constants.DEFAULT_TASK;
        switch (headCmdOptions.size()) {
            case 0:
                break;
            case 1:
                String val = headCmdOptions.get(0);
                String info[] = val.split(":", 2);
                if (info.length == 1) {
                    stage = info[0];
                } else if (info.length == 2) {
                    stage = info[0];
                    if (StringUtils.isNotEmpty(info[1])) {
                        task = info[1];
                    }
                } else {
                }
                break;
            default:
                throw new DeployException("unsupport head no - option than 2");
        }
        log.info("task:{} stage:{}", task, stage);
        appArgs.setTask(task);
        return appArgs;
    }

    public static void main(String[] args) {
        AppArgs res = ArgsParserHelper.parseAppArgs(new String[]{"-h"});
        System.out.println(res);
        System.out.println(ArgsParserHelper.parseAppArgs("-l -h prod".split(" ")));
        System.out.println(ArgsParserHelper.parseAppArgs("-l -h prod -x -a".split(" ")));

        System.out.println(ArgsParserHelper.parseAppArgs("-l -h prod:test -x -a".split(" ")));
        System.out.println(ArgsParserHelper.parseAppArgs("-l -h prod:test:xxf -x -a".split(" ")));
        System.out.println(
            ArgsParserHelper.parseAppArgs("-l -h -ru fasfsa prod:test:xxf -x -a".split(" ")));
        System.out.println(
            ArgsParserHelper.parseAppArgs("-t safsdf -ru fasfsa prod:test:xxf -x -a".split(" ")));
        System.out.println(ArgsParserHelper
            .parseAppArgs("--hosts safsdf -ru fasfsa prod:test:xxf -x -a".split(" ")));
        System.out.println(ArgsParserHelper
            .parseAppArgs("-D a=va --hosts safsdf -ru fasfsa prod:test:xxf -x -a".split(" ")));

    }
}
