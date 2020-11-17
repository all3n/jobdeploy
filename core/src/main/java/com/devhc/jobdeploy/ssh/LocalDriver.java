package com.devhc.jobdeploy.ssh;

import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class LocalDriver extends DeployDriver{
    private static Logger log = Loggers.get();

    public LocalDriver() {
    }

    @Override
    public void execCommand(String command) {
        Runtime r = Runtime.getRuntime();
        String[] commands = {"bash", "-c", command};
        try {
            Process p = r.exec(commands);
            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }

            b.close();
        } catch (Exception e) {
            System.out.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
    }

    @Override
    public void put(String sourceFile, String target){
        execCommand("cp " + sourceFile + " " + target);
    }

    @Override
    public List<Pair<String, Long>> ls(String dir) {
        File file = new File(dir);
        List<Pair<String, Long>> res = Lists.newArrayList();
        Arrays.asList(file.listFiles()).forEach(f -> res.add(Pair.of(f.getName(), f.lastModified())));
        return res;
    }
}


