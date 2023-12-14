package com.devhc.jobdeploy.config;

public class Constants {

  //主版本.次版本.修订号_版本类型(alpha,beta,release)
  public static final String DEPLOY_VERSION = "1.0.7_alpha";
  public static final String DEPLOY_CONFIG_FILENAME = "deploy.json";
  public static final String DEPLOY_SCRIPT_NAME = "deploy";
  public static final String DEPLOY_CONTEXT_FILE = "deploy-context.xml";
  public static final String REMOTE_RELEASE_DIR = "release/commitid";
  public static final String REMOTE_BRANCH_DIR = "release/branch";
  public static final String REMOTE_TASKS_DIR = "release/tasks";
  public static final String REMOTE_TAG_DIR = "release/tag";
  public static final String REMOTE_TIMESTAMP_DIR = "release/timestamp";
  public static final String REMOTE_SAHRE_DIR = "release/share";
  public static final String REMOTE_CURRENT_DIR = "current";
  public static final String CURRENT_REVISION = "REVISION";
  public static final String DEFAULT_TASK = "default";
  public static final String TASK_CLASS_SUFFIX = "Task";
  public static final String DEPLOY_BUILD_CACHE = ".deploy";
  public static final String TASK_BUILD = "build";
  public static final String REMOTE_UPLOAD_TMP = "/tmp/jobdeploy";
  public static final String CUSTOM_CONFIG_DIR = ".jobdeploy";
  public static final String CUSTOM_CONFIG_FILE = "my.properties";
  public static final String HOSTS_ROLE_FILE = "hosts-rules.json";


  public static final Integer JOB_STATUS_OK = 0;
  public static final Integer JOB_STATUS_SKIP = 1;
  public static final Integer JOB_STATUS_ERROR = 2;

}
