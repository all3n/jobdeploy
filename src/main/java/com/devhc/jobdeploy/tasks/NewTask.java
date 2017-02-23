package com.devhc.jobdeploy.tasks;

import ch.ethz.ssh2.SCPClient;
import com.devhc.jobdeploy.App;
import com.devhc.jobdeploy.DeployPlugin;
import com.devhc.jobdeploy.JobTask;
import com.devhc.jobdeploy.annotation.DeployTask;
import com.devhc.jobdeploy.config.Constants;
import com.devhc.jobdeploy.config.DeployJson;
import com.devhc.jobdeploy.config.ScriptTask;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServer;
import com.devhc.jobdeploy.config.structs.DeployServers.DeployServerExecCallback;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.ssh.SSHDriver;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CmdHelper;
import com.devhc.jobdeploy.utils.FileUtils;
import com.google.common.collect.Lists;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@DeployTask
public class NewTask extends JobTask {

  @Autowired
  App app;

  private static Logger log = LoggerFactory.getLogger(NewTask.class);


  @Option(name = "-n",aliases = "--name",usage = "project name")
  private String name;


  @Option(name = "-s", aliases = "--servers",usage = "servers list,use comma, split,like server1xx.xxx.com,server2xx.xxx.com")
  private String servers = "xx1ss.xx.com";


  @Option(name="-r",aliases = "--repository",usage = "repository url,like git@xxxx.com:xxxx/${name}.git")
  private String repository = "git@xxxx.com:xxxx/${name}.git";


  @Option(name="-d",aliases = "--deployto",usage = "deploy to path,like /data/deploy/${name}")
  private String deployTo = "/data/deploy/${name}";

  @Option(name="-m",aliases = "--deploy_mode",usage = "latest or local")
  private String deployMode = "latest";


  @Option(name="-o",aliases = "--overwrite",usage = "overwrite exist config")
  private boolean overwrite = false;



  public void exec() throws Exception {
    File deployJsonFile = new File(Constants.DEPLOY_CONFIG_FILENAME);
    if(deployJsonFile.exists() && !overwrite){
      log.info("{} exist skip write,if you want to overwrite exist please add -o option",Constants.DEPLOY_CONFIG_FILENAME);
      return;
    }

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name",name);
    jsonObject.put("deployto",deployTo);
    jsonObject.put("repository",repository);
    jsonObject.put("deployto",deployTo);
    jsonObject.put("deploy_mode",deployMode);

    String serversSplit[] = servers.split(",");
    List<JSONObject> serversList = Lists.newArrayList();
    for(String server:serversSplit){
      JSONObject serverJson = new JSONObject();
      serverJson.put("server",server);
      serversList.add(serverJson);
    }
    JSONArray serversJson = new JSONArray(serversList);
    jsonObject.put("servers",serversJson);

    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(deployJsonFile)));
    bufferedWriter.write(jsonObject.toString(4));
    bufferedWriter.close();

  }
}
