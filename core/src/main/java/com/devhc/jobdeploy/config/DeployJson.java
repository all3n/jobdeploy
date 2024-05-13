package com.devhc.jobdeploy.config;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.DeployMode;
import com.devhc.jobdeploy.config.parser.JsonArrayParser;
import com.devhc.jobdeploy.config.parser.object.ScriptTaskParser;
import com.devhc.jobdeploy.config.structs.DeployExtension;
import com.devhc.jobdeploy.config.structs.DeployHook;
import com.devhc.jobdeploy.config.structs.DeployServers;
import com.devhc.jobdeploy.exception.DeployException;
import com.devhc.jobdeploy.strategy.Strategy;
import com.devhc.jobdeploy.utils.AnsiColorBuilder;
import com.devhc.jobdeploy.utils.CliHelper;
import com.devhc.jobdeploy.utils.DeployUtils;
import com.devhc.jobdeploy.utils.FileUtils;
import com.devhc.jobdeploy.utils.Loggers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * deploy 配置文件 deploy.json 类
 */
@Component
public class DeployJson extends JSONObject {

  private boolean init = false;

  @Autowired
  public DeployConfig deployConfig;

  private static Logger log = Loggers.get();


  public Properties jobProp = System.getProperties();
  public Map<String, String> env = System.getenv();

  private DeployHook deployHook;

  private DeployServers deployServers;

  @Autowired
  DeployContext deployContext;

  @Autowired
  DeployCustomConfig customConfig;

  private Strategy strategy;

  private Random random = new Random();
  private Map<String, List<DeployExtension>> extensions;

  public JSONArray getServers() {
    return getJSONArray("servers");
  }

  public DeployServers getDeployServers() {
    if (deployServers != null) {
      return deployServers;
    }
    try {
      deployServers = new DeployServers(this);
    } catch (Exception e) {
      throw new DeployException(e);
    }
    return deployServers;
  }

  public String getVersionRequire() {
    return getProperty("version_require", "");
  }

  public Integer getParallel() {
    return this.getInt("parallel", 1);
  }

  public String getChmod() {
    return this.getProperty("chmod", "");
  }

  public String getName() {
    return this.getProperty("name", "application");
  }

  public String getChown() {
    return this.getProperty("chown", "");
  }

  public String getDeployTo() {
    return this.getProperty("deployto", "deploy");
  }

  public String getBranch() {
    return this.getProperty("branch", "");
  }

  public String getKeyFile() {
    List<String> keySearchPath = Lists.newArrayList();
    String user = getUser();
    String workspaceDir = System.getProperty("user.dir");
    String currentUserHome = System.getProperty("user.home");
    String keyFile = this.getProperty("keyfile", null);
    if (StringUtils.isNotEmpty(keyFile)) {
      return keyFile;
    }
    keySearchPath.add(keyFile);
    // for linux
    keySearchPath.add("/home/" + getUser() + "/.ssh/id_rsa");
    keySearchPath.add("/home/" + getUser() + "/.ssh/id_dsa");
    // for not linux
    keySearchPath.add(currentUserHome + "/.ssh/id_rsa");
    keySearchPath.add(currentUserHome + "/.ssh/id_dsa");
    keySearchPath.add(workspaceDir + "/.ssh/" + user + ".key");
    keySearchPath.add(FileUtils.getJarDir() + "/.ssh/" + user + ".key");
    for (String key : keySearchPath) {
      if (key != null) {
        File file = new File(key);
        if (file.exists()) {
          log.info("use key file:{}", key);
          put("keyfile", key);
          return key;
        }
      }
    }
    return null;
  }

  public Boolean getSudo() {
    return getBoolean("sudo", false);
  }

  public String getKeyFilePass() {
    return this.getProperty("keyfilepass", "");
  }

  public Boolean getRemote() {
    return this.getBoolean("remote", false);
  }

  public String getUser() {
    return this.getProperty("user", System.getProperty("user.name"));
  }

  public String getBuildDir() {
    return this.getProperty("build_dir", "");
  }

  public String getRepository() {
    return this.getProperty("repository", null);
  }

  public String getScmUsername() {
    return this.getProperty("scm_username", getUser());
  }

  public String getScmPassword() {
    return this.getProperty("scm_password", getPassword());
  }


  public String getScmAuthtype() {
    // key or password
    return this.getProperty("scm_authtype", "key");
  }

  public String getScmKeyFile() {
    return this.getProperty("scm_keyfile", getKeyFile());
  }

  public String getScmKeyFilePass() {
    return this.getProperty("scm_keyfilepass", getKeyFilePass());
  }

  public String getAuthType() {
    return getProperty("auth_type", "");
  }

  public String getPassword() {
    return this.getProperty("password", customConfig.getCustomConfig("password"));
  }

  public String getAzkabanUser() {
    return this.getProperty("azkaban_user", null);
  }

  public String getAzkabanPassword() {
    return this.getProperty("azkaban_password", null);
  }

  public String getAzkabanJobsPath() {
    return this.getProperty("azkaban_job_path", "jobs");
  }

  public boolean getAzkabanUpload() {
    return this.getBoolean("azkaban_upload", false);
  }

  public String getAzkabanUrl() {
    return this.getProperty("azkaban_url", "");
  }

  public String getDescription() {
    return this.getProperty("description", null);
  }

  public String getMavenParams() {
    return this.getProperty("maven_params", "");
  }

  public String getSvnBranchPath() {
    return this.getProperty("svn_branch_path", "");
  }

  public String getSvnTagPath() {
    return this.getProperty("svn_tag_path", "");
  }

  public String getCurrentLink() {
    return this.getProperty("current_link", "");
  }

  public String getMavenCustomCmd() {
    return getProperty("mvn_custom_cmd", "mvn package");
  }

  public int getKeepReleases() {
    return getInt("keep_releases", 10);
  }


  public DeployMode getDeployMode() {
    String deployMode = this.getProperty("deploy_mode", DeployMode.LOCAL.getName());
    return DeployMode.parse(deployMode);
  }

  public String getScmType() {
    return this.getProperty("scm_type", "git");
  }

  public Strategy getStrategy() {
    if (strategy != null) {
      return strategy;
    }
    this.strategy = Strategy.parse(getProperty("strategy", ""));
    return strategy;
  }

  public String getLocalRepository() {
    return this.getProperty("local_repository", "");
  }

  public String getStage() {
    return this.getProperty("stage", "");
  }

  public JSONArray getNotifyEmail() {
    return getArray("notify_email");
  }

  public String getCustomBuild() {
    return this.getProperty("custom_build", "");
  }

  public Object getUpload() {
    return this.get("upload");
  }

  public String getLinkJarName() {
    return getProperty("link_jar_name", "job.jar");
  }

  public String getTasksDir() {
    return getProperty("task_dir", "tasks");
  }

  public JSONArray getSharedAssets() {
    return getArray("shared_assets");
  }

  public String getPostDeployScript() {
    return getProperty("post_deploy_script", "");
  }


  public String getUploadTarget() {
    return getProperty("upload_target", "");
  }

  public int getSshTimeout() {
    return getInt("ssh_timeout", 120);
  }

  public String getProxy() {
    return getProperty("proxy", customConfig.getCustomConfig("proxy"));
  }

  public String getJumpServer() {
    return getProperty("jump_server", customConfig.getCustomConfig("jump_server"));
  }

  public String getGatewaySecret() {
    return getProperty("jumper_secret", customConfig.getCustomConfig("jumper_secret"));
  }

  public String getGatewayJumper() {
    return getProperty("jumper_server", customConfig.getCustomConfig("jumper_server"));
  }

  public String getGatewaySecretPrefix() {
    return getProperty("jumper_secret_prefix",
        customConfig.getCustomConfig("jumper_secret_prefix"));
  }

  public String getSftpPrefix() {
    return getProperty("sftp_prefix", customConfig.getCustomConfig("sftp_prefix"));
  }

  public JSONArray getTemplates() {
    return getArray("templates");
  }

  public JSONArray getAlerts() {
    return getArray("alerts");
  }

  public String getRemoteTmpPrefix() {
    return getProperty("remote_tmp_prefix", Constants.REMOTE_UPLOAD_TMP);
  }

  public String getSudoUser() {
    return getProperty("sudo_user", null);
  }

  public List<String> getFlows() {
    JSONArray flowsJsonArr = this.getArray("flows");
    List<String> out = Lists.newArrayList();
    if (flowsJsonArr != null) {
      for (int i = 0; i < flowsJsonArr.length(); i++) {
        out.add(flowsJsonArr.getString(i));
      }
    }
    return out;
  }


  public Map<String, ScriptTask> getTasks() {
    JSONArray taskArray = getArray("tasks");
    if (taskArray == null) {
      return null;
    }
    JsonArrayParser<ScriptTask> scriptTaskArrayParser = JsonArrayParser
        .get(ScriptTaskParser.class);
    Map<String, ScriptTask> scriptTaskMap = Maps.newHashMap();
    List<ScriptTask> scriptTaskList = scriptTaskArrayParser.parse(taskArray);
    for (ScriptTask st : scriptTaskList) {
      scriptTaskMap.put(st.getName(), st);
    }
    return scriptTaskMap;
  }

  @Override
  public Object get(String key) throws JSONException {
    if (has(key)) {
      return super.get(key);
    } else {
      return null;
    }
  }

  public DeployHook getHooks() {
    if (this.deployHook != null) {
      return this.deployHook;
    }
    JSONObject hook = getObject("hooks");
    if (hook != null && hook.length() > 0) {
      this.deployHook = new DeployHook(hook);
      return this.deployHook;
    } else {
      return null;
    }
  }


  public Map<String, List<DeployExtension>> getExtensions() {
    if (this.extensions != null) {
      return this.extensions;
    }
    if (!has("extensions")) {
      return null;
    }
    this.extensions = new HashMap<>();
    JSONObject extObjs = getJSONObject("extensions");
    for (Iterator<String> it = extObjs.keys(); it.hasNext(); ) {
      String key = it.next();
      Object obj = extObjs.get(key);
      if (obj.getClass() == String.class) {
        DeployExtension ext = new DeployExtension();
        ext.setClassName(obj.toString());
        ext.setName(key);
        this.extensions.put(key, Collections.singletonList(ext));
      } else if (obj.getClass() == JSONObject.class) {
        JSONObject jObj = (JSONObject) obj;
        DeployExtension ext = new DeployExtension();
        ext.setClassName(jObj.getString("class"));
        ext.setUrl(jObj.optString("url", ""));
        ext.setMd5sum(jObj.optString("md5sum", ""));
        ext.setName(key);
        this.extensions.put(key, Collections.singletonList(ext));
      } else if (obj.getClass() == JSONArray.class) {
        JSONArray jArray = (JSONArray) obj;
        List<DeployExtension> exts = Lists.newArrayList();
        for (int i = 0; i < jArray.length(); i++) {
          JSONObject jObj = (JSONObject) jArray.get(i);
          DeployExtension ext = new DeployExtension();
          ext.setClassName(jObj.getString("class"));
          ext.setUrl(jObj.optString("url", ""));
          ext.setMd5sum(jObj.optString("md5sum", ""));
          ext.setName(key);
          exts.add(ext);
        }
        this.extensions.put(key, exts);
      }
    }
    return extensions;
  }

  public String parseFromEnvOrProperties(String name) {
    if (jobProp.containsKey(name)) {
      return jobProp.getProperty(name);
    }
    String envName =
        name.toUpperCase().replace(".", "_")
            .replace("-", "_");
    envName = "JD_" + envName;
    if (env.containsKey(envName)) {
      return env.get(envName);
    }
    return null;
  }

  @Override
  public boolean getBoolean(String key) throws JSONException {
    return super.getBoolean(key);
  }

  @Override
  public double getDouble(String key) throws JSONException {
    return super.getDouble(key);
  }

  public int getInt(String key, Integer def) throws JSONException {
    String envOrPropValue = parseFromEnvOrProperties(key);
    if (envOrPropValue != null) {
      return Integer.parseInt(envOrPropValue);
    } else if (has(key)) {
      return super.getInt(key);
    } else {
      return def;
    }
  }

  public String getCustom(String key) {
    return customConfig.getCustomConfig(key);
  }

  public String getProperty(String name, String defaultValue) {
    try {
      String value = this.parseFromEnvOrProperties(name);
      if (value != null) {
        put(name, value);
        return value;
      }
      value = DeployUtils.parseRealValue(getString(name), this);
      String ask = CliHelper
          .parseAsk(value, "please input " + name + "?");
      if (ask != null) {
        value = ask;
        this.put(name, ask);
      }

      String customValue = null;
      try {
        customValue = CliHelper
            .parseCustom(value, name, "please input " + name + "?", customConfig);
      } catch (Exception e) {
        throw new DeployException(e);
      }
      if (StringUtils.isNotEmpty(customValue)) {
        value = customValue;
        this.put(name, customValue);
      }

      return value;
    } catch (JSONException e) {
      return defaultValue;
    }
  }

  public JSONObject getObject(String name) {
    if (has(name)) {
      JSONObject value = getJSONObject(name);
      return value;
    } else {
      return null;
    }
  }

  public JSONArray getArray(String name) {
    if (has(name)) {
      JSONArray value = getJSONArray(name);
      return value;
    } else {
      return null;
    }
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    if (!has(name)) {
      return defaultValue;
    }
    return this.getBoolean(name);
  }

  public static JSONObject readJsonFile(String projJsonPath) throws IOException {
    return readJsonFile(projJsonPath, false);
  }

  public static JSONObject readJsonFile(String projJsonPath, boolean existOptional)
      throws DeployException, IOException {
    JSONObject json = null;
    File configJsonFile = new File(projJsonPath);
    if (!configJsonFile.exists()) {
      if (existOptional) {
        return new JSONObject();
      } else {
        throw new DeployException(projJsonPath
            + ":json deploy config file not exists.");
      }
    }
    FileReader fr = new FileReader(configJsonFile);
    BufferedReader br = new BufferedReader(fr);
    String line = null;
    StringBuffer sb = new StringBuffer();
    while ((line = br.readLine()) != null) {
      line = line.trim();
      // ignore comments
      if (line.startsWith("#") || line.startsWith("//")) {
        continue;
      }
      sb.append(line);
    }
    json = new JSONObject(sb.toString());
    br.close();
    fr.close();
    return json;
  }

  public void loadProjectConfigFromJsonString(String json) throws IOException {
    JSONObject stageJson = new JSONObject(json);
    fillJsonInfo(stageJson);
    //force latest
    put("deploy_mode", DeployMode.LATEST.getName());
    initEnvDir();
  }

  public void fillJsonInfo(JSONObject stageJson) {
    Iterator iter = stageJson.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      Object obj = stageJson.get(key);
      put(key, obj);
    }
  }

  public void loadProjectConfig(String stage) throws IOException {
    String projectJsonFileName = Constants.DEPLOY_CONFIG_FILENAME;
    File local = new File(".");
    String projJsonPath = local.getCanonicalFile() + "/"
        + projectJsonFileName;
    if (!(new File(projJsonPath)).exists()) {
      return;
    }
    Boolean existOptional = false;
    if (StringUtils.isNotEmpty(stage)) {
      existOptional = true;
    }
    JSONObject json = readJsonFile(projJsonPath, existOptional);
    fillJsonInfo(json);
    if (StringUtils.isEmpty(stage)) {
      stage = getStage();
    }

    String stageHosts = null;
    // stage deploy.json will overwrite job deploy.json
    // load stage
    if (StringUtils.isNotEmpty(stage)) {
      log.info("start load {} config", stage);
      String stageJsonPath = local.getCanonicalFile() + File.separator + "deploy" + File.separator
          + stage + File.separator + projectJsonFileName;
      JSONObject stageJson = readJsonFile(stageJsonPath);
      fillJsonInfo(stageJson);

      stageHosts = local.getCanonicalFile() + File.separator + "deploy" + File.separator
          + stage + File.separator + "servers.txt";
    }
    put("stage", stage);
    initEnvDir();
    deprecatedCompatibleProperty();
    boolean isLocal = false;

    boolean overwriteByRawHosts = true;
    String argHosts = deployContext.getHosts();
    if (argHosts != null && (argHosts.startsWith("file://") || argHosts.startsWith(".")
        || argHosts.startsWith("/"))) {
      overwriteByRawHosts = false;
      stageHosts = argHosts.replace("file://", "");
    }

    if (stageHosts != null) {
      File stageHostsFile = new File(stageHosts);
      if (stageHostsFile.exists()) {
        fillHosts(stageHostsFile);
      }
    }
    if (overwriteByRawHosts) {
      overwriteByArguments();
    }
    this.varReplace(this);
    JSONArray servers = getServers();
    if (servers != null && servers.length() == 1) {
      Object serverOne = servers.get(0);
      if (serverOne.getClass() == String.class && "local".equals(serverOne.toString())) {
        isLocal = true;
      } else {
        Object serverObj = servers.get(0);
        if (serverObj instanceof JSONObject) {
          JSONObject serverJson = servers.getJSONObject(0);
          if (serverJson != null && "local".equals(serverJson.optString("server", ""))) {
            isLocal = true;
          }
        } else {
          if ("local".equals(serverObj.toString())) {
            isLocal = true;
          }
        }
      }
      log.info("server: {}", serverOne);
    }
    if (isLocal) {
      String homeDirectory = System.getProperty("user.home");
      if (!getDeployTo().startsWith("/")) {
        put("deployto", homeDirectory + File.separator + getDeployTo());
      }
      if (!getCurrentLink().startsWith("/")) {
        put("current_link", homeDirectory + File.separator + getCurrentLink());
      }
    }
  }

  private Object varReplace(Object obj) {
    if (obj.getClass() == String.class) {
      return DeployUtils.parseRealValue((String) obj, this);
    } else if (obj instanceof JSONArray) {
      JSONArray valArr = (JSONArray) obj;
      for (int i = 0; i < valArr.length(); ++i) {
        Object oVarI = valArr.get(i);
        valArr.put(i, varReplace(oVarI));
      }
    } else if (obj instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject) obj;
      for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
        String key = it.next();
        Object val = varReplace(jsonObject.get(key));
        log.debug("replace {}=>{}", key, val);
        jsonObject.put(key, val);
      }
    }
    return obj;
  }

  private void fillHosts(File stageHostsFile) {
    BufferedReader br = null;
    JSONArray servers = new JSONArray();
    try {
      br = IOUtils.toBufferedReader(new FileReader(stageHostsFile));
      String line = null;
      while ((line = br.readLine()) != null) {
        String infos[] = line.split("\\s+");
        if (infos.length > 0) {
          JSONObject obj = new JSONObject();
          obj.put("server", infos[0]);
          for (int i = 1; i < infos.length; ++i) {
            String kv[] = infos[i].split("=");
            if (kv.length == 2) {
              obj.put(kv[0], kv[1]);
            }
          }
          servers.put(obj);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
      IOUtils.closeQuietly(br);
    }
    if (servers.length() > 0) {
      put("servers", servers);
    }
  }

  private void overwriteByArguments() {
    if (StringUtils.isNotEmpty(deployContext.getHosts())) {
      List<String> hosts = Arrays.stream(deployContext.getHosts().split(","))
          .map(String::trim).collect(Collectors.toList());
      put("servers", new JSONArray(hosts));
    }
    deployContext.getAppArgs().getCustomOptions().forEach(this::put);
  }

  private void initEnvDir() {
    if (getDeployMode() == DeployMode.LATEST) {
      String tmpDirBase = deployConfig.getTmpDir();
      File tmpDir;
      if (tmpDirBase != null) {
        tmpDir = new File(
            tmpDirBase + File.separator + getName() + "_" + getUser() + "_" + System
                .currentTimeMillis() + "_"
                + random.nextInt());
        tmpDir.mkdirs();
      } else {
        tmpDir = Files.createTempDir();
      }
      if (StringUtils.isEmpty(getBuildDir())) {
        deployContext.setBuildDir(tmpDir.getPath());
      } else {
        deployContext.setBuildDir(tmpDir.getPath() + File.separator + getBuildDir());
      }
      deployContext.setSrcDir(tmpDir.getPath());
    } else {
      // local mode
      deployContext.setBuildDir(".");

      if (StringUtils.isNotEmpty(getLocalRepository())) {
        deployContext.setSrcDir(File.separator + getLocalRepository());
      } else {
        deployContext.setSrcDir(".");
      }
    }

    deprecatedCompatibleProperty();
    init = true;
  }

  private void deprecatedCompatibleProperty() {
    String strategy = getProperty("strategy", "");
    String newStrategy = "";
    if (strategy.equals("copy")) {
      newStrategy = "maven:copy";
    } else if (strategy.equals("dep")) {
      newStrategy = "maven:assembly";
    } else if (strategy.equals("install")) {
      newStrategy = "maven:install";
    } else if (strategy.equals("package")) {
      newStrategy = "maven:package";
    }

    if (StringUtils.isNotEmpty(newStrategy)) {
      put("strategy", newStrategy);
      log.warn(
          AnsiColorBuilder.red(
              "{} is deprecated,the new value is {},you must change new version,this deprecated property will remove next version"),
          strategy, newStrategy);
    }
    deprecatedPropertyKey("remote_dir", "deployto");

  }

  private void deprecatedPropertyKey(String oldKey, String newKey) {
    if (has(oldKey)) {
      put(newKey, get(oldKey));
      log.warn(
          AnsiColorBuilder.red(
              "{} is deprecated,the new key  is {},you must change new version,this deprecated property will remove next version"),
          oldKey, newKey);
    }
  }

  public String getRemoteTmpUserDir() {
    String remoteTmpPrefix = getRemoteTmpPrefix();
    if (remoteTmpPrefix.endsWith("/")) {
      return remoteTmpPrefix + "jobdeploy-" + getUser() + "/" + deployContext.getDeployid();
    } else {
      return remoteTmpPrefix + "-" + getUser() + "-" + deployContext.getDeployid();
    }
  }

  public String getUserHome() {
    return "/home/" + getUser() + "/";
  }

  public boolean isInit() {
    return init;
  }

  @PreDestroy
  public void shutdown() {
    if (deployServers != null) {
      deployServers.shutdown();

    }
  }

  public File getExecFile(String name) {
    File local = new File(".");
    try {
      String f = local.getCanonicalFile() + File.separator + name;
      return new File(f);
    } catch (IOException e) {
      return null;
    }
  }

  public File getStageFile(String name) {
    File local = new File(".");
    try {
      String f = local.getCanonicalFile() +
          File.separator + "deploy" +
          File.separator + getStage() +
          File.separator + name;
      return new File(f);
    } catch (IOException e) {
      return null;
    }
  }

  public DeployCustomConfig getCustomConfig() {
    return customConfig;
  }
}
