# hooks
* 支持 Before,After  两种 Hooks 方式
* "task":{"before":[],"after":[]}  "task" 为内置task 之一
* hook 指定cmd 默认是在项目的目标目录执行
* 如果需要调用自定义task,需要加前缀@ 
* sample

  ```
    "hooks":{
        "finished":{
            "after":[
                "@restart",
                "sh ./scripts/xxxx.sh",
                "echo xxx"
            ]
        }
    }
  ```
  
