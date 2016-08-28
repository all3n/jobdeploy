package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.scm.ScmCommit;
import com.google.common.collect.Lists;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.scm.svn.SVNKitDriver;
import com.devhc.jobdeploy.svn.structs.SVNDiffHistoryLog;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DeployTask
public class NotifyTask extends JobTask {
  @Autowired
  JavaMailSender mailSender;
  @Autowired
  DeployConfig deployConfig;
  @Autowired
  DeployJson dc;
  @Autowired
  App app;

  private static Logger log = LoggerFactory.getLogger("NotityTask");

  @Override
  public void exec() {
    JSONArray notifys = dc.getNotifyEmail();
    if (notifys != null) {
      log.info("notify email:" + notifys);
      MimeMessage mail = mailSender.createMimeMessage();
      try {
        MimeMessageHelper messageHelper = new MimeMessageHelper(mail,
          true, "utf-8");

        List<String> notifyList = Lists.newArrayList();
        for (int i = 0; i < notifys.length(); i++) {
          notifyList.add(notifys.getString(i));
        }
        messageHelper.setTo(notifyList.toArray(new String[notifyList.size()]));
        String subject;
        if (!dc.getStage().equals("")) {
          subject = "[JobDeploy] deploy " + dc.getName() + " to " + dc.getStage();
        } else if (!dc.getName().equals("")) {
          subject = "[JobDeploy] deploy " + dc.getName();
        } else {
          subject = "[JobDeploy] deploy project success";
        }
        messageHelper.setSubject(subject);// 主题
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        sb.append("Performed a deploy operation on " + sdf.format(date)
          + " <br />");
        sb.append("server:" + dc.getServers() + "<br />");
        if (dc.getRepository() != null) {
          if ("git".equals(dc.getScmType())) {
            gitHistory(sb);
          } else if ("svn".equals(dc.getScmType())) {
            svnHistory(sb);
          }
        }
        messageHelper.setText(sb.toString(), true);// 邮件内容
        mailSender.send(mail);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  private void gitHistory(StringBuffer sb) {
    ScmDriver scm = app.getDeployContext().getScmDriver();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<ScmCommit> history = scm.history();
    for (ScmCommit h : history) {
      Date d = new Date();
      d.setTime(Long.valueOf(h.getCommitTime() * 1000l));
      String timeStr = sdf.format(d);
      String msg = String.format("[%s](%s)%s:%s", h.getCommitId(),
        timeStr, h.getEmail(), h.getMessage());
      sb.append(msg + "<br />");
    }
  }

  protected void svnHistory(StringBuffer sb) {
    sb.append("<hr />");
    SVNKitDriver svnDriver = new SVNKitDriver(dc.getRepository(),
      dc.getScmUsername(), dc.getScmPassword());
    ArrayList<SVNDiffHistoryLog> logList = svnDriver
      .getLastestUpdateHistory(-1, -1);
    SVNDiffHistoryLog logDiff = logList.get(0);

    sb.append(logDiff.getFormatLog());

    if (logDiff.getIssueId() > 0) {
      String issueUrl = deployConfig.getProperty("issue_url").replace(
        "{issue_id}", String.valueOf(logDiff.getIssueId()));
      sb.append("<br />相关issue：<a href='" + issueUrl + "'>" + issueUrl
        + "</a><br />");
    }

    if (logDiff.getReviewId() > 0) {
      String reviewUrl = deployConfig.getProperty("review_url").replace(
        "{review_id}", String.valueOf(logDiff.getReviewId()));
      sb.append("相关review：<a href='" + reviewUrl + "'>" + reviewUrl
        + "</a><br />");
    }
  }

}
