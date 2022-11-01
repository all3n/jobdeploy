## 默认内置task

* default flow:
    * validate
    * scm
    * build
    * upload
    * uploadScript
    * symlink
    * shareAssets
    * uploadJob
    * clean
    * notify
    * finished
* other task
    * upDep
    * remote 查看 远程服务器 上 共有commitid
    * rollback 回滚远程服务器上commitid
    * exec 执行外部task 
    * new 创建一个deploy.json
    
## 自定义task
* "tasks" : ["cmd":["service xx restart"],"name":"restart"] 
* name,cmd 必填 dir 可选 默认是部署current 目录
* dir 如果以/开头则是绝对路径,如果不是则是current 下相对路径
* 执行:  deploy stage:exec:taskName   如果调用默认stage 可以省略stage deploy :exec:taskName
  

 

## 内置task 执行
* deploy [stage]:taskName [options]
* 如果没有stage或者使用默认stage，deploy :upload 这样调用
* groovy task:如果taskfile 后缀为.groovy 则调用groovy engine 进行加载
* 同时.groovy file 必须 实现 DeployPlugin 接口
* 示例如下 tasks/example.groovy=>调用 deploy :exec -local -tf example.groovy
``` 
    import com.devhc.jobdeploy.DeployPlugin;
    import com.devhc.jobdeploy.App;
    public class Foo implements DeployPlugin{
            @Override
            public void run(App app){
                    println app.getDeployJson().getName();
            }
    }
    
```
