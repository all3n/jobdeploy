package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.*;

import groovy.lang.Tuple2;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * this task is used to clean history release dir
 *
 * @author wanghch
 */
@DeployTask
public class CleanTask extends JobTask {

  private static Logger log = Loggers.get();
  @Autowired
  DeployJson dc;
  @Autowired
  StrategyManager sm;
  @Autowired
  App app;

  public void exec() throws Exception {
    if (dc.getKeepReleases() > 0) {
      dc.getDeployServers().exec(new DeployServers.DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServers.DeployServer server) throws Exception {
          String deployTo = server.getDeployto();
          String releaseUploadDir = deployTo + "/" + app.getDeployContext().getReleseDir() + "/..";

          log.info("scan {},keep release:{}", releaseUploadDir, dc.getKeepReleases());
          final List<Tuple2<String, Long>> entryList = Lists.newArrayList();
          final List<Tuple2<String, Long>> preFilterList = server.getDriver().ls(releaseUploadDir);

          for (Tuple2<String, Long> e : preFilterList) {
            if (!e.getFirst().equals(".") && !e.getFirst().equals("..")) {
              entryList.add(e);
            }
          }

          Collections.sort(entryList, (e1, e2) -> e2.getSecond() - e1.getSecond() < 0 ? -1 : (e2.getSecond() - e1.getSecond() > 0 ? 1 : 0));

          for (Tuple2<String, Long> e : entryList) {
            log.info("dir:{} mtime:{} datetime:{}", e.getFirst(), e.getSecond(),
                    DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(e.getSecond()));
          }

          if (dc.getKeepReleases() < entryList.size()) {
            List<Tuple2<String, Long>> entryListSlice = entryList
                    .subList(dc.getKeepReleases(), entryList.size());
            for (Tuple2<String, Long> e : entryListSlice) {
              Date date = new Date(e.getSecond());
              log.info("remove {} datetime:{} mtime:{} dir:{}", server.getServer(),
                      DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date), e.getSecond(),
                      e.getFirst());
              String rmCmd = "rm -rf " + releaseUploadDir + "/" + e.getFirst();
              server.getDriver().execCommand(rmCmd);
            }
          }
        }
      });
    }
  }
}
