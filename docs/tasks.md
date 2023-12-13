## Default Built-in Tasks

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
* other tasks:
    * upDep
    * remote: View commit IDs on remote server
    * rollback: Rollback commit ID on remote server
    * exec: Execute external task
    * new: Create a deploy.json file

## Custom Tasks
* ```
  "tasks" : [
  	{"name":"restart","cmd":["echo $PWD"]}
  ] 
  ```

* name and cmd are required, dir is optional and default is the current deployment directory.

* If dir starts with "/", it is an absolute path; otherwise, it is a relative path under the current directory.

* Execution: deploy stage:exec:taskName. If using the default stage, you can omit "stage" and use deploy :exec:taskName.
  
## Custom Flow Pipeline

```
"tasks" : [
          {"name":"echo_pwd","cmd":["echo $PWD"]},
          {"name":"ls_files","cmd":["ls -al ./*", "sleep 3", "echo $HOSTNAME"]},
          {"name":"exec_py","cmd":["python python/main.py"]}
 ],
 "flows": [
    "build"
    ,"upload"
    ,"symlink"
    ,"shareAssets"
    ,"uploadJob"
    ,"clean"
    ,"notify"
    ,"echo_pwd"
    ,"ls_files"
    ,"exec_py"
  ]

```
## Executing Commands

```
deploy stage:exec -c "ls -al"
```
 

## Built-in Task Execution
* deploy [stage]:taskName [options]
* If no stage is specified or the default stage is used, you can call it as deploy :upload
* Groovy task: If the taskfile has the extension .groovy, it will be loaded using the Groovy engine
* The .groovy file must implement the DeployPlugin interface
* Example: tasks/example.groovy => call deploy :exec -local -tf example.groovy
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
