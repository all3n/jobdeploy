package com.devhc.jobdeploy.config;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.DeployMode;
import com.devhc.jobdeploy.config.parser.JsonArrayParser;
import com.devhc.jobdeploy.config.parser.object.ScriptTaskParser;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    public String getChmod() {
        return this.getProperty("chmod", "775");
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

    public String getPassword() {
        return this.getProperty("password", null);
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
        return this.getBoolean("azkaban_upload", true);
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
        return getInt("keep_releases", 20);
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

        // stage deploy.json will overwrite job deploy.json
        if (StringUtils.isNotEmpty(stage)) {
            log.info("start load {} config", stage);
            String stageJsonPath = local.getCanonicalFile() + "/deploy/"
                    + stage + "/" + projectJsonFileName;
            JSONObject stageJson = readJsonFile(stageJsonPath);
            fillJsonInfo(stageJson);
        }
        put("stage", stage);

        initEnvDir();

        deprecatedCompatibleProperty();
        overwriteByArguments();
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
                        tmpDirBase + "/" + getName() + "_" + getUser() + "_" + System
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
                deployContext.setSrcDir("./" + getLocalRepository());
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
        return Constants.REMOTE_UPLOAD_TMP + "-" + getUser() + "-" + deployContext.getDeployid();
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
}
