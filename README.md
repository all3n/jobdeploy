# jobdeploy

## 配置文件
    deploy.json
   
## 使用方法
```
deploy [headOptions] [stage]:task [taskOptions]
HeaderOptions:
 -V (--verbose)            : print verbose for debug error stacktrace (default:
                             false)
 -h (--help)               : print help usage (default: true)
 -l (--list)               : print task list (default: false)
 -ru (--repositoryUrl) VAL : url of repository
 -v (--version)            : print deploy version (default: false)
 -y (--yes)                : default yes to deploy (default: false)
17:23:32 [INFO]:TaskOptions:
 -b (--branch) VAL  : branch you want to deploy
 -d (--delete)      : delete if commitid is same (default: false)
 -t (--tags) VAL    : tag you want to deploy
 -uj (--uploadJobs) : upload job to azkaban (default: false)
```
## 配置参数

|  字段名 	| 类型  	| 必填  | 用法  	| 默认值 | 
|---	|---	|---	|---	| --- |
|  servers 	|  array 	| Yes  |部署服务器 [{server,chown,chmod,deployto}]  	| |
|  strategy	|   string	| Yes 	|  部署策略 参看下面详细说明 | |
| maven_params | string| No | maven 额外参数|
|  deployto	|  string 	| No  	|  部署默认路径，如果servers 不写deployto时使用  	| |
|  chmod	| string  	|  No 	|   部署文件夹权限	| 775 |
|  chown	|  string 	|  No 	|  部署目录用户组 	|  |
| name	|   string	|   No	|   项目名称	| application|
| keyfile	|   string	|  No 	| ssh key 位置  | |
| keyfilepass	|  string 	|  No 	|   ssh key 密码 | |
| user	|   string	| No	|  ssh 用户名 	| 当前shell登陆用户 |
| password	|  string	|  No 	|  ssh 密码 	| 如果设置该值，会采用ssh 用户名密码方式，否则则采用ssh key 方式 |
| repository	| string  	|  No 	|   资源库地址 	| 没有 默认读取 deploy.json 文件所在 repository 地址|
| scm_keyfile | string | No | scm key 地址 | 默认使用ssh key |
| scm_keyfilepass	|  string 	|   No	|   资源库git ssh key密码，auth type 为key 时使用	|  |
| scm_username	|  string 	|  No	|  资源库用户名 scm_authtype 为password 使用 	| |
| scm_password	|  string 	|   No	|   资源库密码 scm_authtype 为password 使用	| |
| scm_type	|  string 	|  No  	| 资源库类型   	| git |
| scm_authtype | string | No | scm 验证类型 (key,password) |  key |
| stage	|  string 	| No  	| 默认stage  	| |
| sudo	|boolean  	| No   	| sudo:true  必须是nopassword 方式，尽量避免使用sudo 	| false |
| upload	| array   	|  No 	|  需要上传目录  	| 如果需要分stage 目录 可以在script 名称前加@,指定stage 为 用 stage:script 方式指定 |
| notify_email	| string   	| No  	| 部署完邮件通知 逗号分隔   	| |
| hooks	| json  	|  No 	|   task:{before:['cmd1','cmd2',....],after:[]} ${deployto}代表目标路径	| |
| deploy_mode | String | No | 默认部署方式 (local,latest) 测试用local,线上建议使用latest | local |
| custom_build	|   string	| No  	|   自定义build 命令	| |
| branch	|   string	| No  	|  部署分支名 	| |
| task_dir	|  string 	|  No 	|  扩展task目录 	| tasks |
| azkaban_url | string | No | azkaban server url | 默认读取 conf/deploy_config.properties 配置中 |
| azkaban_user	|  string 	| No  	|   azkaban 用户名	| |
| azkaban_password	| string  	| No   	|   azkaban 密码	| |
| azkaban_job_path	| string  	| No   	|   azkaban jobs 配置目录	| 默认在项目代码 jobs 文件夹下 |
| description	|   string  |No	|   azkaban project 描述	| |
| local_repository	| String 	|  No 	| 只有当.git 不在项目目录 比如在上层 填写 ..   	| 默认当前目录找不到会找上一层 |
| build_dir	| String 	|  No 	|  当代码目录下有多个项目，用来指定build的文件夹 	| 默认会根据本地.git 在上层，会使用当前文件夹名去查找build |
| shared_assets | Array | No | [{from:,to,create:true}] from 如果以/开头 则认为是current 目录下，to 目标路径，create 可选 目标路径不存在创建  默认true ||
| link_jar_name	| String 	|  No 	| 目标部署目录对jar 软链接名 	| job.jar |
| post_deploy_script | String | No | post deploy script path,if empty or script not exist will skip | |
| current_link | String | No | current symlink target path,ensure has permission |  |

## 部署策略 strategy

* maven 类型
	* maven:copy 上传为package，dep 包需要 执行deploy -t upDep 单独上传
	* maven:assembly 使用assembly:assembly 打包
	* maven:install 执行mvn install
	* maven:package 使用 package 打包 (如果使用shade 插件打成依赖包也用这个)
	* maven:assembly:archive   (使用assembly:single 使用assembly.xml 方式 生成 tar.gz 方式)
* script类型
	* code 脚本语言以及非编译类语言
	
* 其他类型 
	* ant
	* composer (for php)
	* custom 自定义build cmd，使用 custom_build 作为 build 命令
	
	
## 部署模式 deploy_mode

* local
	* 使用本地代码作为部署
	* 这种方式是将deploy.json 配置放在代码目录下
* latest
	* 使用资源库代码部署
	* 这种方式是将deploy.json 放在外部目录下

## SSH KEY && SCM KEY
1. 默认ssh key 和 scm key 一样
2. 如果scm key 不一样 需要单独设置scm_keyfile
3. 如需要为你的项目创建单独deploy scm key 可以通过ssh-keygen 生成一个单独的ssh key(注意文件位置不要把本身用户ssh key 误覆盖了)
4. 通过添加.ssh/config 设置指定host使用不同的key 如下 

	```
	   Host git.xxx.com
		 HostName git.xxx.com
		 IdentitiesOnly yes
		 IdentityFile ~/.ssh/id_rsa_xxx	
	```
5. 设置配置文件权限 chmod 600  ~/.ssh/config

## Azkaban Job Upload

* 在项目目录下添加jobs 目录

```
文件夹结构如下:
--jobdir
    --jobs
        --project1
            a.job
        --project2
            b.job
```

* 执行 deploy [stage]:uploadJob 即可，会自动根据文件夹名创建project，并upload workflow
* 如果想要在deploy 过程包含上传jobs 可以 通过添加-uj 参数
* 配置：
	* 全局默认配置：deploy_config.properties 配置 azkaban.url=https://azkaban.xxx.com
	* 也可以为每个deploy 配置单独azkaban 地址 deploy.json 中 azkaban_url
	* azkaban_user,azkaban_password 在需要上传jobs 项目这个是必须要填的

## 默认内置task
* default flow:
	* validate
	* scm
	* build
	* upload
	* uploadScript
	* symlink
	* shareAssets
	* uploadJob
	* clean
	* notify
	* finished
* other task
	* upDep
	* remote 查看 远程服务器 上 共有commitid
	* rollback 回滚远程服务器上commitid
	* exec 执行外部task 
	
	
* task 执行
	* deploy [stage]:taskName [options]
	* 如果没有stage或者使用默认stage，deploy :upload 这样调用
	
	* groovy task:如果taskfile 后缀为.groovy 则调用groovy engine 进行加载
	* 同时.groovy file 必须 实现 DeployPlugin 接口
	
	* 示例如下 tasks/example.groovy=>调用 deploy :exec -local -tf example.groovy
	
	```	
	import com.devhc.jobdeploy.DeployPlugin;
	import com.devhc.jobdeploy.App;
	public class Foo implements DeployPlugin{
	        @Override
	        public void run(App app){
	                println app.getDeployJson().getName();
	        }
	}
	
	```
	
	
## 分stage部署

```
	建立deploy文件夹，然后在其中分别建立不同stage文件夹，并新增deploy.json
stage下的deploy.json会merge项目的deploy.json

文件夹结构如下:
--jobdir
    deploy.json
    --deploy
        --sandbox
            deploy.json
        --prod
            deploy.json
            
            
	如果想script upload 也能分stage 可以在upload 参数 分stage 文件名前加@ （since 4.1.1）
	即： upload:['@stageScript'] ,其中default 为如果没有对应stage 文件夹 ，将会采用default 文件夹下的文件
	目录结构可以这样
--jobdir
	--stageScript
		--stage1
			- stage1 scripts
		--stage2
			- stage2 scripts
		--default
			- default scripts

	如果想使用指定stage 文件 可以这样写 upload:['stage1:stageScript'] 通过冒号分割 stage script 来指定script
	


```


部署到 prod: deploy prod [options]


# json 变量引用
* json 字符串变量 可以引用其他字符串属性

# 更新部署工具
* 使用deploy --update 可以重新下载最新代码build更新

## 内置函数
* CLI::ask()
* CLI::ask("请输入 xxx?")
* CLI::custom() ask once,second time will load from ~/.jobdeploy/my.properties
* CLI::custom('custom.key') same as CLI::custom()   custom.key is key in my.properties

## 编译使用
* 安装
	* git clone https://github.com/all3n/jobdeploy.git
	* cd jobdeploy
	* ./install
	
* 注意：	
	* 默认 install /opt/jobdeploy 下,如果要安装其他 目录 ./install /path/to
	* 安装之后jar包放在输入位置，script会软连接 deploy 放在/usr/bin/deploy下
* 安装后更新
	* deploy --update 


## Sample

### git

```
---deploy.json
{
  'repository': 'git@xxx.xxx.xxx.xxx:xxx/deploy-test.git',
  'name': 'deploy-test',
  'strategy': 'maven:package',
  'upload': [
    'service',
    'scripts',
    '@config'
  ],
  'notify_email': [
    'wanghch@mvad.com'
  ]
}

---deploy
	--- dg
		--- deploy.json
			{
			  "user": "xxx",
			  "keyfilepass": "",
			  "deployto": "/data/deploy/deploy-test",
			  "scm_keyfile": "/home/xxx/.ssh/id_rsa_xxx",
			  "servers": [
			    {
			      "server": "wexxdg.xxx.com"
			    },
			    {
			      "server": "wexxdg.xxx.com"
			    }
			  ],
			 "deploy_mode": "latest",
			 "shared_assets": [
			    {
			      "from": "logs",
			      "to": "/data1/logs/deploy-test/"
			    },
			    {
			      "from": "data",
			      "to": "/data1/app-data/deploy-test/"
			    }
			  ]
			}

		
	--- ss
		--- deploy.json
			{
			  "user": "xxx",
			  "keyfilepass": "",
			  "deployto": "/data/deploy/deploy-test",
			  "scm_keyfile": "/home/xxx/.ssh/id_rsa_xxx",
			  "servers": [
			    {
			      "server": "wexxss.xxx.com"
			    },
			    {
			      "server": "wexxss.xxx.com"
			    }
			  ],
			 "maven_params": "-Pcdh5-jdk8",
			 "deploy_mode": "latest",
			 "shared_assets": [
			    {
			      "from": "logs",
			      "to": "/data/logs/deploy-test/"
			    },
			    {
			      "from": "data",
			      "to": "/data/app-data/deploy-test/"
			    }
			  ]
			}
```


### svn
```
---deploy.json
	{
	  "strategy": "maven:assembly:archive",
	  "stage": "adhoc",
	  "repository": "https://svn.xxx.xxx.com/svn/abc/trunk/deploy-test/",
	  "svn_tag_path": "/svn/abc/tags/{tag}/deploy-test/",
	  "svn_branch_path": "/svn/abc/branches/{branch}/deploy-test/",
	  "scm_type": "svn",
	  "scm_username": "CLI::ask()",
	  "scm_password": "CLI::ask()",
	  "link_jar_name": "deploy-test.jar",
	  "post_deploy_script": ""
	}
	--- deploy
		--- adhoc
			--- deploy.json
			{
			  "deployto": "opt/deploy-test",
			  "servers": [
			    {
			      "server": "xxx.xxx.xxx.com"
			    },
			  ],
			  "deploy_mode": "local",
			  "maven_params": "-Prelease-profile -DskipTests=true",
			  "shared_assets": [
			    {
			      "from": "logs",
			      "to": "/home/${user}/app-data/logs/deploy-test",
			      "chmod": "755"
			    },
			    {
			      "from": "data",
			      "to": "/home/${user}/app-data/data/deploy-test",
			      "chmod": "755"
			    },
			    {
			      "from": "model-training.jar",
			      "to": "/home/${user}/opt/dep-lib/dep-lib-0.0.1-SNAPSHOT.jar",
			      "create": false
			    }
			  ]
			}

			

```


## 常见问题
1. 无法创建文件夹

	* 使用不同账户，执行执行脚本时候sudo经常会碰到 sudo: sorry, you must have a tty to run sudo这个情况，其实修改一下sudo的配置就好了
	* vi /etc/sudoers 
	* 注释掉 Default requiretty 一行
	* 意思就是sudo默认需要tty终端。注释掉就可以在后台执行了。

2. 无法使用sshkey

	* .ssh 权限
	* authorized_keys 600
	* .ssh 700
	
	
3. Permission denied
	
	* 如果使用账号不具有创建目标部署文件夹目录权限导致
	* 如果你部署user 为 deploy 你要部署到 /target/a1
	* 可以通过 chgrp deploy /target,chmod 775 /target 修改目标目录上层目录group 权限
