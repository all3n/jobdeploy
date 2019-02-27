package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.sftp.ErrorCodes;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@DeployTask
public class RollbackTask extends JobTask {

  private static Logger log = Loggers.get();

  @Option(name = "-b", usage = "branch you want to rollback", aliases = "--branch")
  private String branch;

  @Option(name = "-t", usage = "tag you want to rollback", aliases = "--tags")
  private String tag;

  @Option(name = "-cid", usage = "commitId you want to rollback", aliases = "--commitId")
  private String commitId;

  @Autowired
  DeployJson json;

  @Autowired
  App app;

  @Autowired
  JavaMailSender mailSender;

  @Override
  public void exec() throws Exception {
    String rollback = "";
    if (StringUtils.isNotEmpty(commitId)) {
      log.info("start rollback to commit:{}", commitId);
      rollback = commitId;
    } else if (StringUtils.isNotEmpty(tag)) {
      log.info("start rollback to tag:{}", tag);
      rollback = tag;
    } else if (StringUtils.isNotEmpty(branch)) {
      log.info("start rollback to branch:{}", branch);
      rollback = branch;
    } else {
      throw new DeployException("must set rollback name (commit|tag|branch)");
    }

    json.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server)
          throws Exception {
        String rollbackDir = getRollbackDir();

        String deployTo = server.getDeployto();

        ensureRollbackDirExists(deployTo + "/" + rollbackDir, server.getDriver().getSftpClient());
        server.getDriver().symlink(deployTo, rollbackDir, Constants.REMOTE_CURRENT_DIR);
      }
    });

    JSONArray notifys = json.getNotifyEmail();
    if (notifys != null) {

      MimeMessage mail = mailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(mail,
          true, "utf-8");
      messageHelper.setSubject("[JobDeploy] Rollback " + json.getName() + " to " + rollback);// 主题
      List<String> notifyList = Lists.newArrayList();
      for (int i = 0; i < notifys.length(); i++) {
        notifyList.add(notifys.getString(i));
      }
      log.info("start to send rollback email:{}", StringUtils.join(notifyList, ","));
      messageHelper.setTo(notifyList.toArray(new String[notifyList.size()]));
      StringBuffer sb = new StringBuffer();
      SimpleDateFormat sdf = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");
      Date date = new Date();

      sb.append("Performed a rollback operation on " + json.getName() + "  " + sdf.format(date)
          + " <br />");
      sb.append("server:" + json.getServers() + "<br />");
      messageHelper.setText(sb.toString(), true);
      mailSender.send(mail);
    }

  }

  protected void ensureRollbackDirExists(String release,
      SFTPv3Client sftpClient) {
    try {
      sftpClient.ls(release);
    } catch (SFTPException se) {
      switch (se.getServerErrorCode()) {
        case ErrorCodes.SSH_FX_NO_SUCH_FILE:
          throw new DeployException(release + " not exists");
        default:
          throw new DeployException(se);
      }
    } catch (IOException e) {
      throw new DeployException(e);
    }
  }

  protected String getRollbackDir() {
    String release = null;
    if (StringUtils.isEmpty(branch) && StringUtils.isEmpty(tag)) {
      release = Constants.REMOTE_RELEASE_DIR + "/" + commitId;
    } else if (StringUtils.isNotEmpty(tag)) {
      release = Constants.REMOTE_TAG_DIR + "/" + tag;
    } else if (StringUtils.isNotEmpty(branch)) {
      release = Constants.REMOTE_BRANCH_DIR + "/" + branch;
    } else {
      throw new DeployException("invalid rollback dir");
    }
    return release;
  }

}
