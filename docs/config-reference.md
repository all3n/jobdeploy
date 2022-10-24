# Config Reference

|  字段名   | 类型      | 必填  | 用法      | 默认值 |
|---    |---    |---    |---    | --- |
|  servers  |  array    | Yes  |部署服务器 [{server,chown,chmod,deployto}]      | |
|  strategy |   string  | Yes   |  部署策略 参看下面详细说明 | |
| maven_params | string| No | maven 额外参数||
|  deployto |  string   | No    |  部署默认路径，如果servers 不写deployto时使用     | |
|  chmod    | string    |  No   |   部署文件夹权限  | 775 |
|  chown    |  string   |  No   |  部署目录用户组   |  |
| name  |   string  |   No  |   项目名称    | application|
| keyfile   |   string  |  No   | ssh key 位置  | |
| keyfilepass   |  string   |  No   |   ssh key 密码 | |
| user  |   string  | No    |  ssh 用户名   | 当前shell登陆用户 |
| password  |  string   |  No   |  ssh 密码     | 如果设置该值，会采用ssh 用户名密码方式，否则则采用ssh key 方式 |
| auth_type   |  string   |  No   |   认证类型 | 默认为空根据上下文检测 |
| repository    | string    |  No   |   资源库地址  | 没有 默认读取 deploy.json 文件所在 repository 地址|
| scm_keyfile | string | No | scm key 地址 | 默认使用ssh key |
| scm_keyfilepass   |  string   |   No  |   资源库git ssh key密码，auth type 为key 时使用   |  |
| scm_username  |  string   |  No   |  资源库用户名 scm_authtype 为password 使用    | |
| scm_password  |  string   |   No  |   资源库密码 scm_authtype 为password 使用 | |
| scm_type  |  string   |  No   | 资源库类型    | git |
| scm_authtype | string | No | scm 验证类型 (key,password) |  key |
| stage |  string   | No    | 默认stage     | |
| sudo  |boolean    | No    | sudo:true  必须是nopassword 方式，尽量避免使用sudo    | false |
| upload    | array     |  No   |  需要上传目录     | 如果需要分stage 目录 可以在script 名称前加@,指定stage 为 用 stage:script 方式指定 |
| notify_email  | string    | No    | 部署完邮件通知 逗号分隔       | |
| hooks | json      |  No   |   task:{before:['cmd1','cmd2',....],after:[]} ${deployto}代表目标路径 | |
| deploy_mode | String | No | 默认部署方式 (local,latest) 测试/CI用local, 独立部署用latest | local |
| custom_build  |   string  | No    |   自定义build 命令    | |
| branch    |   string  | No    |  部署分支名   | |
| task_dir  |  string   |  No   |  扩展task目录     | tasks |
| azkaban_url | string | No | azkaban server url | 默认读取 conf/deploy_config.properties 配置中 |
| azkaban_user  |  string   | No    |   azkaban 用户名  | |
| azkaban_password  | string    | No    |   azkaban 密码    | |
| azkaban_job_path  | string    | No    |   azkaban jobs 配置目录   | 默认在项目代码 jobs 文件夹下 |
| description   |   string  |No |   azkaban project 描述    | |
| local_repository  | String    |  No   | 只有当.git 不在项目目录 比如在上层 填写 ..    | 默认当前目录找不到会找上一层 |
| build_dir | String    |  No   |  当代码目录下有多个项目，用来指定build的文件夹    | 默认会根据本地.git 在上层，会使用当前文件夹名去查找build |
| shared_assets | Array | No | [{from:,to,create:true}] from 如果以/开头 则认为是current 目录下，to 目标路径，create 可选 目标路径不存在创建  默认true ||
| link_jar_name | String    |  No   | 目标部署目录对jar 软链接名    | job.jar |
| post_deploy_script | String | No | post deploy script path,if empty or script not exist will skip | |
| current_link | String | No | current symlink target path,ensure has permission |  |
| keep_releases | int | No | keep history release num | 20|
| tasks | Array[] | No | {"cmd":[""],"name":"taskName","dir":"/path/execdir"}  dir 不写默认为项目部署current 目录 name,cmd 必填| |



## json 变量引用
* json 字符串变量 可以引用其他字符串属性
* 比如:"repository": "git@xxx:xxx${name}.git"



## 内置函数
* CLI::ask()
* CLI::ask("请输入 xxx?")
* CLI::custom() ask once,second time will load from ~/.jobdeploy/my.properties
* CLI::custom('custom.key') same as CLI::custom()   custom.key is key in my.properties


## 示例

1. "user":"CLI::custom()"
   1. 这样会在首次deploy 时候提示输入 user 值,在完成后会将user 存入 ~/.jobdeploy/my.properties 下次deploy 会直接引用不会提示输入
1. 如果想不同项目在相同key 上 custom 配置不同,可以通过custom key 方式区分
   1. "user":"CLI::custom('xxx.user')"
   1. 这样在部署会提示输入 xxx.user 的值,并且在结束会以xxx.user 存入配置


## 绝对路径 && 相对路径
1. deploy 配置中 以/开头会被认为绝对路径,如果不以/开头会认为 相对 /home/${user} 目录下

## 环境变量支持
属性可以通过JD_开头环境变量覆盖(名称都大写)
比如a_b 可以被 JD_A_B 覆盖



## auth 顺序
1. PASSWORD (password 非空)
2. KEY_FILE (keyfile 非空)
3. SSH_AGENT (SSH-Agent Sock ENV Exists)
4. PAGEANT (PAGEANT Sock ENV Exists)

CI 容器环境可以通过mount ssh-agent socket 实现认证，
本地一般使用password/keyfile 认证

socks5 proxy: 可以通过在~/.jobdeploy/my.properties
添加 proxy=sockproxy.server:proxyport  支持


## 全局配置
1. conf/deploy_config.properties
   1. azkaban.url   azkaban url
   1. local.tmp.dir   部署使用临时目录 如果不设置默认使用tmp, 一般tmp 空间太小时候设置
   1. maven.bin.path  设置本地多个maven, 制定maven mvn bin 路径



