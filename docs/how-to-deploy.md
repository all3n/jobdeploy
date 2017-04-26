# HOW TO DEPLOY

## scm  branch,tag 部署
* deploy prod -t v1.0  部署v1.0 tag 版本 到prod 环境上
* deploy prod -b b1.0  部署b1.0 branch 版本 到prod 环境上
* deploy prod   部署master 版本 到prod 环境上
* deploy prod -r commitid   指定commitid 部署到prod 环境上(svn 为version 号,git 为commitid hash string)
