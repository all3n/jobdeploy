package com.devhc.jobdeploy.notify;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.scm.ScmCommit;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.scm.svn.SVNKitDriver;
import com.devhc.jobdeploy.svn.structs.SVNDiffHistoryLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;

/**
 * @TODO
 */
public class EmailNotify extends BaseNotify {

  @Override
  public void alert(App app, JSONObject alertObj) {
  }


  private void gitHistory(App app, StringBuffer sb) {
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

  protected void svnHistory(App app, StringBuffer sb) {
    DeployConfig deployConfig = app.getConfig();
    DeployJson dc = app.getDeployJson();
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
