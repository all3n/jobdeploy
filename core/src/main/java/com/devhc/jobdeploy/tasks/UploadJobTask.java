package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.azkaban.client.Account;
import com.devhc.jobdeploy.azkaban.client.AzkabanClient;
import com.devhc.jobdeploy.azkaban.client.Project;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.Loggers;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class UploadJobTask extends JobTask {

  private static Logger log = Loggers.get();
  @Autowired
  DeployJson dc;

  @Autowired
  DeployConfig jobdeploy;

  @Autowired
  App app;

  @Override
  public void exec() throws Exception {
    String buildDir = app.getDeployContext().getBuildDir();
    if (!app.getDeployContext().isUploadJob() && !app.getDeployContext().getAppArgs().getTask()
        .equals("uploadJob") && !dc.getAzkabanUpload()) {
      log.info("skip job upload");
      status = Constants.JOB_STATUS_SKIP;
      return;
    }

    log.info(AnsiColorBuilder.yellow("start to upload azkaban jobs"));
    String jobsDirPath = buildDir + File.separator + dc.getAzkabanJobsPath().replace("/", File.separator);
    log.info("jobsDirPath:{}", jobsDirPath);
    File jobsDir = new File(jobsDirPath);

    File[] dirs = jobsDir.listFiles();
    if (dirs == null) {
      status = Constants.JOB_STATUS_SKIP;
      log.warn(AnsiColorBuilder.red("jobs dir is not exists,skip upload jobs"));
      return;
    }
    Account account = new Account();
    account.setUsername(dc.getAzkabanUser());
    account.setPassword(dc.getAzkabanPassword());

    String url = dc.getAzkabanUrl();
    if (StringUtils.isEmpty(url)) {
      url = jobdeploy.getAzkabanUrl();
    }

    if (StringUtils.isEmpty(url)) {
      throw new DeployException(
          "azkaban url is not set please check local/global config azkaban.url is set");

    }

    for (File dir : dirs) {
      if (dir.isDirectory()) {
        Project project = new Project();
        if (StringUtils.isNotEmpty(dc.getDescription())) {
          project.setDescription(dc.getDescription());
        } else {
          project.setDescription(dir.getName());
        }
        project.setLocation(jobsDirPath);
        project.setName(dir.getName());

        AzkabanClient client = new AzkabanClient(project, account, url);
        client.upload();
      } else {
        log.warn("{} is dir,skip", dir);
      }
    }

  }
}
