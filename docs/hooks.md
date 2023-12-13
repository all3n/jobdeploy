# Hooks

* Supports two types of hooks: "Before" and "After."
* The structure for defining hooks is as follows: "task":{"before":[],"after":[]}, where "task" is one of the built-in tasks.
* The "hook" field specifies the command to be executed. By default, it is executed in the target directory of the project.
* If you need to call a custom task, you need to prefix it with "@".
* Sample:

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
