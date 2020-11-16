package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import groovy.lang.Tuple2;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class RemoteTask extends JobTask {

  private static Logger log = Loggers.get();
  @Autowired
  App app;
  @Autowired
  DeployJson dc;

  @Override
  public void exec() throws Exception {
    printUnionCommit(Constants.REMOTE_RELEASE_DIR, "commit");
    printUnionCommit(Constants.REMOTE_BRANCH_DIR, "branches");
    printUnionCommit(Constants.REMOTE_TAG_DIR, "tags");
  }

  private void printUnionCommit(final String subDir, String name) throws Exception {
    final Map<String, List<String>> commitMap = Maps.newHashMap();
    final Map<String, Integer> commitUnion = Maps.newHashMap();
    int length = dc.getDeployServers().getLength();
    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server) throws Exception {
        SSHDriver driver = (SSHDriver) server.getDriver();
        List<String> commitList = Lists.newArrayList();
        String deployTo = server.getDeployto();
        String release = deployTo + "/" + subDir;
        if (!driver.exists(release)) {
          return;
        }
        List<Tuple2<String, Long>> files = driver.getSftpClient().ls(release);
        for (Tuple2<String, Long> f : files) {
          if (".".equals(f.getFirst()) || "..".equals(f.getFirst())) {
            continue;
          }
          commitList.add(f.getFirst());
          if (commitUnion.containsKey(f.getFirst())) {
            commitUnion.put(f.getFirst(), commitUnion.get(f.getFirst()) + 1);
          } else {
            commitUnion.put(f.getFirst(), 1);
          }
        }

        commitMap.put(server.getServer(), commitList);
      }
    });
    if (commitMap.isEmpty()) {
      return;
    }

    log.info("server {}", name);
    for (Entry<String, List<String>> entry : commitMap.entrySet()) {
      log.info(AnsiColorBuilder.green(entry.getKey()));
      for (String commitId : entry.getValue()) {
        log.info("\t" + AnsiColorBuilder.cyan(commitId));
      }
    }
    log.info("avaiable union {}", name);
    for (Entry<String, Integer> entry : commitUnion.entrySet()) {
      if (entry.getValue().equals(length)) {
        log.info("\t" + AnsiColorBuilder.red(entry.getKey()));
      }
    }
  }
}
