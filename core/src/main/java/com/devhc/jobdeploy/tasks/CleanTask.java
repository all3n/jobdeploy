package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.manager.StrategyManager;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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

          log.debug("scan {},keep release:{}", releaseUploadDir, dc.getKeepReleases());
          final List<Pair<String, Long>> entryList = Lists.newArrayList();
          final List<Pair<String, Long>> preFilterList = server.getDriver().ls(releaseUploadDir);

          for (Pair<String, Long> e : preFilterList) {
            if (!e.getKey().equals(".") && !e.getKey().equals("..")) {
              entryList.add(e);
            }
          }

          Collections.sort(entryList, (e1, e2) -> e2.getValue() - e1.getValue() < 0 ? -1 : (e2.getValue() - e1.getValue() > 0 ? 1 : 0));

          if(log.isDebugEnabled()){
            for (Pair<String, Long> e : entryList) {
              log.debug("dir:{} mtime:{} datetime:{}", e.getKey(), e.getValue(),
                  DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(e.getValue()));
            }
          }

          if (dc.getKeepReleases() < entryList.size()) {
            List<Pair<String, Long>> entryListSlice = entryList
                    .subList(dc.getKeepReleases(), entryList.size());
            for (Pair<String, Long> e : entryListSlice) {
              Date date = new Date(e.getValue());
              log.debug("remove {} datetime:{} mtime:{} dir:{}", server.getServer(),
                      DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date), e.getValue(),
                      e.getKey());
              String rmCmd = "rm -rf " + releaseUploadDir + "/" + e.getKey();
              server.getDriver().execCommand(rmCmd);
            }
          }
        }
      });
    }
  }
}
