# 分环境 stage部署

```
  建立deploy文件夹，然后在其中分别建立不同stage文件夹，并新增deploy.json
stage下的deploy.json会merge项目的deploy.json

文件夹结构如下:
--jobdir
    deploy.json
    --deploy
        --sandbox
            deploy.json
        --prod
            deploy.json
            
            
    如果想script upload 也能分stage 可以在upload 参数 分stage 文件名前加@ （since 4.1.1）
    即： upload:['@stageScript'] ,其中default 为如果没有对应stage 文件夹 ，将会采用default 文件夹下的文件
    目录结构可以这样
--jobdir
    --stageScript
        --stage1
            - stage1 scripts
        --stage2
            - stage2 scripts
        --default
            - default scripts

    如果想使用指定stage 文件 可以这样写 upload:['stage1:stageScript'] 通过冒号分割 stage script 来指定script
    


```


部署到 prod: deploy prod [options]
