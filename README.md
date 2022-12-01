# jobdeploy

[![Build Status](https://travis-ci.org/all3n/jobdeploy.svg?branch=master)](https://travis-ci.org/all3n/jobdeploy)

## What's is Jobdeploy
jobdeploy is a full java implement automation cli tool deploy over SSH Protocol,it config by JSON and less Dependencies



## install[recommand]

replace JD_HOME with your deploy path

```
JD_HOME=$HOME/opt/jobdeploy && mkdir -p $JD_HOME/bin && cd $JD_HOME/bin && wget -O deploy https://raw.githubusercontent.com/all3n/jobdeploy/master/script/deploy && chmod +x ./deploy && ./deploy --update
```



## install [source]

```
git clone https://github.com/all3n/jobdeploy.git
cd jobdeploy
./install $HOME/opt/jobdeploy
```




### bash

```
echo 'export PATH=$HOME/opt/jobdeploy/bin:$PATH ' >> ~/.bashrc
```
### zsh
```
echo 'export PATH=$HOME/opt/jobdeploy/bin:$PATH ' >> ~/.zshrc
```

## Contents
* [Config Reference](docs/config-reference.md)
* [Stage Configuration](docs/stages.md)
* [git/svn](docs/samples.md)
* [FAQ](docs/faq.md)
* [How To Deploy](docs/how-to-deploy.md)
* [Tasks](docs/tasks.md)
* [Deploy Strategy](docs/strategy.md)
* [Deploy_mode](docs/deploy-mode.md)
* [Authentication](docs/authentication.md)
* [SharedAssets](docs/SharedAssets.md)
* [templates](docs/templates.md)
* [Notify](docs/notify.md)

## Support
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)
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
