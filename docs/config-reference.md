# Config Reference

| Field Name        | Type     | Required | Usage                                | Default Value                                             |
| ----------------- | -------- | -------- | ------------------------------------ | --------------------------------------------------------- |
| servers           | array    | Yes      | Deployment servers [{server,chown,chmod,deployto}] |                                                           |
| strategy          | string   | Yes      | Deployment strategy, see detailed explanation below |                                                           |
| maven_params      | string   | No       | Additional Maven parameters           |                                                           |
| deployto          | string   | No       | Default deployment path. Used when "deployto" is not specified in "servers" |                                                           |
| chmod             | string   | No       | Folder permissions for deployment     | 775                                                       |
| chown             | string   | No       | User group for the deployment directory |                                                           |
| name              | string   | No       | Project name                         | application                                               |
| keyfile           | string   | No       | SSH key location                      |                                                           |
| keyfilepass       | string   | No       | SSH key password                      |                                                           |
| user              | string   | No       | SSH username                          | Current shell login user                                  |
| sudo_user         | string   | No       | SSH username for remote sudo access. Only supported in jumper server mode |                                                           |
| password          | string   | No       | SSH password. If set, SSH username and password will be used instead of SSH key |                                                           |
| auth_type         | string   | No       | Authentication type. If empty, it will be detected based on the context |                                                           |
| repository        | string   | No       | Repository address                    | If not specified, it will default to the repository address where the deploy.json file is located |
| scm_keyfile       | string   | No       | SCM key location                      | Default: using SSH key                                     |
| scm_keyfilepass   | string   | No       | SCM git SSH key password. Used when "auth_type" is "key" |                                                           |
| scm_username      | string   | No       | SCM username. Used when "scm_authtype" is "password" |                                                           |
| scm_password      | string   | No       | SCM password. Used when "scm_authtype" is "password" |                                                           |
| scm_type          | string   | No       | SCM type                              | git                                                       |
| scm_authtype      | string   | No       | SCM authentication type (key, password) | key                                                       |
| stage             | string   | No       | Default stage                         |                                                           |
| sudo              | boolean  | No       | Whether to use sudo. If true, it must be passwordless sudo. Avoid using sudo if possible | false                                                     |
| upload            | array    | No       | Directories to upload                 | If you need to specify a stage directory, prefix the script name with "@" and specify the stage using "stage:script" |
| notify_email      | string   | No       | Email addresses for deployment notification (comma-separated) |                                                           |
| hooks             | json     | No       | Task hooks: {before: ['cmd1', 'cmd2', ...], after: []}. Use ${deployto} to represent the target path |                                                           |
| deploy_mode       | string   | No       | Default deployment mode (local, latest). Use "local" for testing/CI and "latest" for independent deployment | local                                                     |
| custom_build      | string   | No       | Custom build command                  |                                                           |
| branch            | string   | No       | Deployment branch name                |                                                           |
| task_dir          | string   | No       | Extended task directory               | tasks                                                     |
| azkaban_url       | string   | No       | Azkaban server URL                    | Default: read from conf/deploy_config.properties           |
| azkaban_user      | string   | No       | Azkaban username                      |                                                           |
| azkaban_password  | string   | No       | Azkaban password                      |                                                           |
| azkaban_job_path  | string   | No       | Azkaban jobs configuration directory  | Default: jobs folder in the project code                   |
| description       | string   | No       | Azkaban project description           |                                                           |
| local_repository  | string   | No       | Local repository address. Use ".." if the .git folder is not in the project directory | Default: Look in the current directory, and if not found, look in the parent directory |
| build_dir         | string   | No       | When there are multiple projects in the code directory, specify the build folder | Default: Use the current folder name to search for the build folder based on the local .git folder in the parent directory |
| shared_assets     | array    | No       | Shared assets configuration [{from, to, create:true}]. If "from" starts with "/", it is considered relative to the current directory. "to" is the target path, and "create" (optional) indicates whether to create the target path if it doesn't exist. Default: true |
| link_jar_name     | string   | No       | Soft link name for the target deployment directory | job.jar                                                   |
| post_deploy_script | string | No       | Post-deployment script path. If empty or the script does not exist, it will be skipped |                                                           |
| current_link      | string   | No       | Target path for the current symlink. Ensure it has the necessary permissions |                                                           |
| keep_releases     | int      | No       | Number of historical releases to keep | 20                                                        |
| tasks             | array[]  | No       | {"cmd":[""],"name":"taskName","dir":"/path/execdir"} If "dir" is not specified, it defaults to the project's current deployment directory. "name" and "cmd" are required |                                                           |
| parallel          | int      | No       | Parallelism                           | 1                                                         |
| templates         | array[object] | No    | Template definition: {src, dest, mode} |                                                           |
| code_patterns     | object: includes:[] excludes:[]   | No| only work in code strategy for compress code tgz|



## JSON Variable Reference
* JSON string variables can reference other string properties.
* For example: "repository": "git@xxx:xxx${name}.git"

## Built-in Functions
* CLI::ask()
* CLI::ask("Please enter xxx?")
* CLI::custom() - asks once, the second time it loads from ~/.jobdeploy/my.properties
* CLI::custom('custom.key') - same as CLI::custom(), where 'custom.key' is a key in my.properties

## Examples

1. "user":"CLI::custom()"
   1. This will prompt for the input value of "user" during the initial deployment. Once completed, the "user" value will be stored in ~/.jobdeploy/my.properties, and subsequent deployments will directly use the stored value without prompting for input.
2. If you want different projects to have different custom configurations for the same key, you can differentiate them using custom keys.
   1. "user":"CLI::custom('xxx.user')"
   2. This will prompt for the input value of "xxx.user" during deployment and store it as "xxx.user" in the configuration after completion.

## Absolute Path & Relative Path
1. In the deployment configuration, a path starting with "/" is considered an absolute path. If it doesn't start with "/", it is considered relative to the /home/${user} directory.

## Environment Variable Support
Properties can be overridden by environment variables starting with "JD_" (all uppercase). For example, "a_b" can be overridden by "JD_A_B".

## Authentication Order
1. PASSWORD (if password is not empty)
2. KEY_FILE (if keyfile is not empty)
3. SSH_AGENT (if SSH-Agent Sock ENV exists)
4. PAGEANT (if PAGEANT Sock ENV exists)

In CI container environments, authentication can be achieved by mounting the SSH-Agent socket. For local environments, password/keyfile authentication is commonly used.

Socks5 Proxy: You can add "proxy=sockproxy.server:proxyport" to ~/.jobdeploy/my.properties to enable support.

## Hosts
1. The "servers" configuration can define stage-specific directories in the stage file.
2. The format of the "servers.txt" file is: the first column is the server name, followed by key=value parameters (e.g., server a=1 b=2).
3. When "servers.txt" exists, the JSON "servers" configuration is ignored.
4. "servers.txt" can also be specified using the "-H" or "--hosts" option.

## Host Rules
```
{
    "sftp":{
        "prod": {"pattern": "*.prod.xxxx.com", "prefix": "/Default/prod-xxxx"},
        "default" : {"pattern":"*", "prefix": "/Default"}
    }
}
```

## Variable Reference
```
{
"var_name": "test",
  "test_var" : "test ${var_name}",
  "embed_map" : {
      "a" : "${test_var}"
  },
  "list_var" : [
      "a", "${test_var}"
  ]
}
```
## Variable Injection by Environment
```
"${env:TEST_NAME}"
```
## Variable Injection by Environment with Default Value
```
"${env:TEST_NAME:default_value}"
```

## Variable Injection by Argument
```
deploy stage -D var_name=xxxx
```

## Variable Injection by Environment
```
Use JD_ prefix for variables and capitalize words, replace "." with underscore "_"
user.name -> JD_USER_NAME
```

## Global Configuration
1. conf/deploy_config.properties
   1. azkaban.url - Azkaban URL
   2. local.tmp.dir - Temporary directory used for deployment. If not set, it defaults to "tmp". It is recommended to set it when the available space in the "tmp" directory is limited.
   3. maven.bin.path - Specify the path to the mvn binary for multiple local Maven installations.
