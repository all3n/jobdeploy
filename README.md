# jobdeploy

[![Build Status](https://travis-ci.org/all3n/jobdeploy.svg?branch=master)](https://travis-ci.org/all3n/jobdeploy)

## What's is Jobdeploy
jobdeploy is a automation cli tool deploy over SSH,it config by JSON and less Dependencies


## Quick Start
1. create deploy.json in directory
	1. deploy :new -d '/data/deploy/${name}' -m latest -n xxx -r 'git@xxxx.com:xxx/${xxx}.git' -s xxx.xxx.com
1. run by docker ssh-agent
    1. docker run -u$(id -u):$(id -g) -v$HOME/.m2:/home/jobdeploy/.m2 -v$(pwd):/src -v$SSH_AUTH_SOCK:/var/run/ssh-agent.sock -e SSH_AUTH_SOCK=/var/run/ssh-agent.sock -e JD_USER=$USER --rm -it jobdeploy:0.0.1-SNAPSHOT  test
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
