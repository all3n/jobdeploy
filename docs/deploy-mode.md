# 部署模式 deploy_mode

* local
    * 使用本地代码作为部署
    * 这种方式是将deploy.json 配置放在代码目录下
    * 开发方式建议使用
    * local 是默认deploy_mode
* latest
    * 使用资源库代码部署
    * 这种方式是将deploy.json 放在外部目录下
    * 线上建议使用latest
