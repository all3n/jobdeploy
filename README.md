# jobdeploy

[![Build Status](https://travis-ci.org/all3n/jobdeploy.svg?branch=master)](https://travis-ci.org/all3n/jobdeploy)

## What's is Jobdeploy
jobdeploy is a automation cli tool deploy over SSH,it config by JSON and less Dependencies


## Quick Start
1. create deploy.json in directory
	2. deploy :new -d '/data/deploy/${name}' -m latest -n xxx -r 'git@xxxx.com:xxx/${xxx}.git' -s xxx.xxx.com
2. deploy


## Contents
* [安装Install](docs/install.md)
* [deploy.json Config Reference](docs/config-reference.md)
* [分环境 stage 配置](docs/stages.md)
* [样例 samples](docs/samples.md)
* [常见问题 FAQ](docs/faq.md)
* [How To Deploy](docs/how-to-deploy.md)
* [Tasks](docs/tasks.md)
* [Strategy 部署策略](docs/strategy.md)
* [部署模式 deploy_mode](docs/deploy-mode.md)
* [身份认证 Authentication](docs/authentication.md)

## Support
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)


## Runtime Dependencies
* maven
* java
