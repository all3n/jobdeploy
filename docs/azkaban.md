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



```
部署代码同时上传azkaban job:
deploy stage -uj

单独部署jobs：
deploy stage:uploadJob
```

