package com.devhc.jobdeploy.tasks;

import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@DeployTask
public class TemplateTask extends JobTask {
  private static final Logger LOG = Loggers.get();
  @Autowired
  DeployJson dc;

  @Autowired
  App app;

  @Override
  public void exec() throws Exception {
    JSONArray templates = dc.getTemplates();
    if (templates == null || templates.length() == 0) {
      return;
    }
    File stageTmpDir = dc.getStageFile("templates");
    File varsText = dc.getStageFile("vars.txt");
    File tmpDir = dc.getExecFile("templates");
    if(stageTmpDir == null){
      return;
    }
    Map<String, Object> context = Maps.newHashMap();
    if(varsText != null && varsText.exists()){
      List<String> lines = FileUtils.readLines(varsText, StandardCharsets.UTF_8);
      for(String line: lines){
        String info[] = line.trim().split("=");
        if(info.length == 2){
          context.put(info[0], info[1]);
        }
      }
    }
    Jinjava jinjava = new Jinjava();
    jinjava.setResourceLocator(new ResourceLocator() {
      @Override
      public String getString(String s, Charset charset, JinjavaInterpreter jinjavaInterpreter)
          throws IOException {
        File stageFile = new File(stageTmpDir.getAbsolutePath() + File.separator + s);
        File tmpFile = new File(tmpDir.getAbsolutePath() + File.separator + s);
        if(stageFile.exists()){
          return FileUtils.readFileToString(stageFile, StandardCharsets.UTF_8);
        }else if(tmpFile.exists()){
          return FileUtils.readFileToString(tmpFile, StandardCharsets.UTF_8);
        }else {
          return null;
        }
      }
    });
    context.put("name", dc.getName());
    for (int i = 0; i < templates.length(); ++i) {
      JSONObject tmp = (JSONObject) templates.get(i);
      String src = tmp.getString("src");
      String dest = tmp.getString("dest");
      String mode = tmp.optString("mode");
      File srcFile = new File(stageTmpDir.getAbsolutePath() + File.separator + src);
      if(srcFile.exists()) {
      }else if(tmpDir != null){
        srcFile = new File(tmpDir.getAbsolutePath() + File.separator + src);
      }else{
        throw new DeployException(src + " not found in templates dir");
      }
      LOG.info("template: {}", srcFile);
      File finalSrcFile = srcFile;
      String tmpContent = FileUtils.readFileToString(srcFile, StandardCharsets.UTF_8);
      dc.getDeployServers().exec(new DeployServerExecCallback() {
        @Override
        public void run(DeployJson dc, DeployServer server) throws Exception {
          Map<String, Object> serverCtx = new HashMap<>(context);
          String fName = finalSrcFile.getName();
          Map<String, String> args = server.getArgs();
          serverCtx.put("server", server.getServer());
          serverCtx.putAll(args);
          File tFile = Files.createTempFile(fName, ".txt").toFile();
          String result = jinjava.render(tmpContent, serverCtx);
          FileUtils.writeStringToFile(tFile, result, StandardCharsets.UTF_8);
          String releaseDest = server.getDeployto() + "/" + app.getDeployContext().getReleseDir() + "/" + dest;
          server.getDriver().put(tFile.getAbsolutePath(), releaseDest);
          if(StringUtils.isNotEmpty(mode)){
            server.getDriver().changePermission(dest, mode, "");
          }
          tFile.delete();
        }
      });
    }

  }
}
