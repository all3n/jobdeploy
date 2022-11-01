# jobdeploy

[![Build Status](https://travis-ci.org/all3n/jobdeploy.svg?branch=master)](https://travis-ci.org/all3n/jobdeploy)

## What's is Jobdeploy
jobdeploy is a automation cli tool deploy over SSH,it config by JSON and less Dependencies

## install[recommand]
```
JD_HOME=$HOME/opt/jobdeploy && mkdir -p $JD_HOME/bin && cd $JD_HOME/bin && wget -O deploy https://raw.githubusercontent.com/all3n/jobdeploy/master/script/deploy && chmod +x ./deploy && ./deploy --update
```

## install [source]
```
git clone https://github.com/all3n/jobdeploy.git
cd jobdeploy
./install
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

## Support
* [Azkaban](docs/azkaban.md)
* [hooks](docs/hooks.md)
* [OS Support](docs/os.md)


## Runtime Dependencies
* maven
* java > 1.8

## Update
```
deploy --update
```

## [Samples](https://github.com/all3n/jobdeploy/tree/master/samples)
1. deploy --hosts xxx.xxx.xx test

