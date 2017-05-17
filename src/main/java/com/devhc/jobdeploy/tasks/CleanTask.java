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
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
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

          String releaseUploadDir = deployTo + "/" + Constants.REMOTE_RELEASE_DIR;
          SFTPv3Client sftpClient = server.getDriver().getSftpClient();

          log.info("scan {},keep release:{}", releaseUploadDir, dc.getKeepReleases());
          final List<SFTPv3DirectoryEntry> entryList = Lists.newArrayList();
          List<SFTPv3DirectoryEntry> preFilterList = sftpClient.ls(releaseUploadDir);

          for (SFTPv3DirectoryEntry e : preFilterList) {
            if (!e.filename.equals(".") && !e.filename.equals("..")) {
              entryList.add(e);
            }
          }

          Collections.sort(entryList, new Comparator<SFTPv3DirectoryEntry>() {
            @Override
            public int compare(SFTPv3DirectoryEntry o1, SFTPv3DirectoryEntry o2) {
              return o2.attributes.mtime - o1.attributes.mtime;
            }
          });

          for (SFTPv3DirectoryEntry e : entryList) {
            log.info("dir:{} mtime:{} datetime:{}", e.filename, e.attributes.mtime,
                DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(e.attributes.mtime * 1000l));
          }

          if (dc.getKeepReleases() < entryList.size()) {
            List<SFTPv3DirectoryEntry> entryListSlice = entryList
                .subList(dc.getKeepReleases(), entryList.size());
            for (SFTPv3DirectoryEntry e : entryListSlice) {
              Date date = new Date(e.attributes.mtime * 1000l);
              log.info("remove {} datetime:{} mtime:{} dir:{}", server.getServer(),
                  DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date), e.attributes.mtime,
                  e.filename);
              String rmCmd = "rm -rf " + releaseUploadDir + "/" + e.filename;
              server.getDriver().execCommand(rmCmd);
            }
          }

        }
      });
    }
  }
}
