package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class ShareAssetsTask extends JobTask {

  private static Logger log = Loggers.get();

  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  @Override
  public void exec() throws Exception {
    dc.getDeployServers().exec(new DeployServerExecCallback() {
      @Override
      public void run(DeployJson dc, DeployServer server) throws Exception {
        JSONArray assets = dc.getSharedAssets();
        if (assets == null) {
          return;
        }

        /**
         * a(from) ----> b(to)
         * == ln -sfT b a 
         */
        for (int i = 0; i < assets.length(); i++) {
          JSONObject sharedDir = assets.getJSONObject(i);
          if (!sharedDir.has("from") || !sharedDir.has("to")) {
            throw new DeployException(" ShareAssetsTask must has from,to");
          }
          String from = sharedDir.getString("from");
          String deployTo = server.getDeployto();

          from = DeployUtils
              .addPrefixIfPathIsRelative(from, deployTo + "/" + Constants.REMOTE_CURRENT_DIR + "/");
          String to = sharedDir.getString("to");

          to = DeployUtils.parseRealValue(to, dc);
          to = DeployUtils.addPrefixIfPathIsRelative(to, dc.getUserHome());
          from = DeployUtils.parseRealValue(from, dc);
          boolean createTarget = true;
          if (sharedDir.has("create")) {
            createTarget = sharedDir.getBoolean("create");
          }
          String chmod = sharedDir.optString("chmod", "777");
          if (createTarget) {
            server.getDriver().mkdir(to, chmod, dc.getChown());
          } else {
            server.getDriver().changePermission(to, chmod, dc.getChown(), false);
          }

          String cmd = "ln -sfT " + to + " " + from;
          server.getDriver().execCommand(cmd);
        }
      }
    });
  }

}
