# HOW TO DEPLOY
```
mode:
latest：
会拉取代码
当前路径只有deploy配置,没有代码
会动态拉取代码部署

local:
不会拉取代码，直接用deploy 命令所在目录代码进行部署
试用CI/本地开发环境部署
```

## scm  branch,tag 部署
```
 该方式使用latest方式，会拉取代码

  deploy prod -t v1.0  部署v1.0 tag 版本 到prod 环境上
  deploy prod -b b1.0  部署b1.0 branch 版本 到prod 环境上
  deploy prod   部署master 版本 到prod 环境上
  deploy prod -r commitid   指定commitid 部署到prod 环境上(svn 为version 号,git 为commitid hash string)

```

## 部署指定机器
* deploy --hosts xx.xxx.xx.com test


## 部署自定义参数
* deploy --hosts xx.xxx.xx.com test -D auth_type=password

## CI环境部署
```
CI 环境建议直接用local方式因为CI容器默认已经下载好当前代码
同时加上-y 跳过yes确认

deploy -y stage
```


