package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class LocalDriver extends DeployDriver {
    private static Logger log = Loggers.get();

    public LocalDriver() {
    }

    @Override
    public void execCommand(String command) {
        String[] commands = {"bash", "-c", command};
        CmdHelper.execCmdArr(commands, ".", log);
    }

    @Override
    public void put(String sourceFile, String target) {
        execCommand("cp " + sourceFile + " " + target);
    }

    @Override
    public List<Pair<String, Long>> ls(String dir) {
        File file = new File(dir);
        List<Pair<String, Long>> res = Lists.newArrayList();
        File[] files = file.listFiles();
        if (files != null) {
            Arrays.asList(files).forEach(f -> res.add(Pair.of(f.getName(), f.lastModified())));
        }
        return res;
    }
}


