package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployConfig;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.notify.BaseNotify;
import com.devhc.jobdeploy.notify.EmailNotify;
import com.devhc.jobdeploy.notify.HttpNotify;
import com.devhc.jobdeploy.scm.ScmCommit;
import com.devhc.jobdeploy.scm.ScmDriver;
import com.devhc.jobdeploy.scm.svn.SVNKitDriver;
import com.devhc.jobdeploy.svn.structs.SVNDiffHistoryLog;
import com.devhc.jobdeploy.utils.HttpClientHelper;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@DeployTask
public class NotifyTask extends JobTask {
  @Autowired
  DeployConfig deployConfig;
  @Autowired
  DeployJson dc;
  @Autowired
  App app;
  private static Logger log = Loggers.get();

  @Override
  public void exec() {
    JSONArray alerts = dc.getAlerts();
    if (alerts != null) {
      for (int i = 0; i < alerts.length(); ++i) {
        if (dc.has(alerts.getString(i))) {
          JSONObject alertObj = dc.getObject(alerts.getString(i));
          String alertType = alertObj.getString("type");
          BaseNotify notify = null;
          if ("http".equals(alertType)) {
            notify = new HttpNotify();
          } else if ("email".equals(alertType)) {
            notify = new EmailNotify();
          }
          if (notify != null) {
            notify.alert(app, alertObj);
          }
        }
      }
    }
  }
}
