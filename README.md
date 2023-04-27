# jobdeploy

[![Maven](https://github.com/all3n/jobdeploy/actions/workflows/maven.yml/badge.svg)](https://github.com/all3n/jobdeploy/actions/workflows/maven.yml)
[![Docker](https://github.com/all3n/jobdeploy/actions/workflows/docker-image.yml/badge.svg)](https://hub.docker.com/r/jobdeploy/jobdeploy/tags)
[![Version](https://img.shields.io/github/v/release/all3n/jobdeploy?style=social)](https://github.com/all3n/jobdeploy/releases)

## What's Jobdeploy
jobdeploy is a full java implement automation cli tool deploy over SSH Protocol,it config by JSON and less Dependencies

## Install[recommand]
replace JD_HOME with your deploy path

```
JD_HOME=$HOME/opt/jobdeploy && mkdir -p $JD_HOME/bin && cd $JD_HOME/bin && wget -O deploy https://raw.githubusercontent.com/all3n/jobdeploy/master/script/deploy && chmod +x ./deploy && ./deploy --update
```
## Install [source]

```
git clone https://github.com/all3n/jobdeploy.git
cd jobdeploy
./install $HOME/opt/jobdeploy
```


## Contents
* [Config Reference](docs/config-reference.md)
* [Stage Configuration](docs/stages.md)
* [SCM git/svn](docs/samples.md)
* [How To Deploy](docs/how-to-deploy.md)
* [Tasks](docs/tasks.md)
* [Deploy Strategy](docs/strategy.md)
* [Deploy Mode](docs/deploy-mode.md)
* [Authentication](docs/authentication.md)
* [SharedAssets](docs/SharedAssets.md)
* [Templates](docs/templates.md)
* [Notify](docs/notify.md)
* [FAQ](docs/faq.md)

## Support
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)
* [Proxy](docs/proxy.md)
* [OS Support](docs/os.md)
* [docker](docs/docker.md)
* [CI](docs/ci.md)


## Runtime Dependencies
* maven
* java  1.8

## Update
```
deploy --update
```
## [Samples](https://github.com/all3n/jobdeploy/tree/master/samples)
