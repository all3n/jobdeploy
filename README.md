# jobdeploy

[![Build Status](https://travis-ci.org/all3n/jobdeploy.svg?branch=master)](https://travis-ci.org/all3n/jobdeploy)

## What's is Jobdeploy
jobdeploy is a automation cli tool deploy over SSH,it config by JSON and less Dependencies
## install
JD_HOME=/opt/jobdeploy && mkdir -p $JD_HOME/bin && cd $JD_HOME/bin && wget -O deploy https://raw.githubusercontent.com/all3n/jobdeploy/master/script/deploy && chmod +x ./deploy && ./deploy --update


## Quick Start
1. create deploy.json in directory
	1. deploy :new -d '/data/deploy/${name}' -m latest -n xxx -r 'git@xxxx.com:xxx/${xxx}.git' -s xxx.xxx.com
1. run by docker ssh-agent
    1. docker run -u$(id -u):$(id -g) -v$HOME/.m2:/home/jobdeploy/.m2 -v$(pwd):/src -v$SSH_AUTH_SOCK:/var/run/ssh-agent.sock -e SSH_AUTH_SOCK=/var/run/ssh-agent.sock -e JD_USER=$USER --rm -it jobdeploy:0.0.1-SNAPSHOT  test
2. deploy


## Contents
* [Install](docs/install.md)
* [Config Reference](docs/config-reference.md)
* [Stage Configuration](docs/stages.md)
* [Samples](docs/samples.md)
* [FAQ](docs/faq.md)
* [How To Deploy](docs/how-to-deploy.md)
* [Tasks](docs/tasks.md)
* [Deploy Strategy](docs/strategy.md)
* [Deploy_mode](docs/deploy-mode.md)
* [Authentication](docs/authentication.md)

## Support
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)


## Runtime Dependencies
* maven
* java
