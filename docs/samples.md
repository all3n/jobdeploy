# Sample

## git



```
java maven 示例
---deploy.json
{
  'repository': 'git@xxx.xxx.xxx.xxx:xxx/deploy-test.git',
  'name': 'deploy-test',
  'strategy': 'maven:package',
  'upload': [
    'service',
    'scripts',
    '@config'
  ],
  'notify_email': [
    'xxx@xxxx.com'
  ]
}

---deploy
    --- test 
        --- deploy.json
            {
              "user": "xxx",
              "keyfilepass": "",
              "deployto": "/data/deploy/deploy-test",
              "scm_keyfile": "/home/xxx/.ssh/id_rsa_xxx",
              "servers": [
                {
                  "server": "wexxdg.xxx.com"
                },
                {
                  "server": "wexxdg.xxx.com"
                }
              ],
             "deploy_mode": "latest",
             "shared_assets": [
                {
                  "from": "logs",
                  "to": "/data1/logs/deploy-test/"
                },
                {
                  "from": "data",
                  "to": "/data1/app-data/deploy-test/"
                }
              ]
            }

        
    --- prod 
        --- deploy.json
            {
              "user": "xxx",
              "keyfilepass": "",
              "deployto": "/data/deploy/deploy-test",
              "scm_keyfile": "/home/xxx/.ssh/id_rsa_xxx",
              "servers": [
                {
                  "server": "wexxss.xxx.com"
                },
                {
                  "server": "wexxss.xxx.com"
                }
              ],
             "maven_params": "-Pcdh5-jdk8",
             "deploy_mode": "latest",
             "shared_assets": [
                {
                  "from": "logs",
                  "to": "/data/logs/deploy-test/"
                },
                {
                  "from": "data",
                  "to": "/data/app-data/deploy-test/"
                }
              ]
            }
```


## svn
```
---deploy.json
    {
      "strategy": "maven:assembly:archive",
      "stage": "adhoc",
      "repository": "https://svn.xxx.xxx.com/svn/abc/trunk/deploy-test/",
      "svn_tag_path": "/svn/abc/tags/{tag}/deploy-test/",
      "svn_branch_path": "/svn/abc/branches/{branch}/deploy-test/",
      "scm_type": "svn",
      "scm_username": "CLI::ask()",
      "scm_password": "CLI::ask()",
      "link_jar_name": "deploy-test.jar",
      "post_deploy_script": ""
    }
    --- deploy
        --- adhoc
            --- deploy.json
            {
              "deployto": "opt/deploy-test",
              "servers": [
                {
                  "server": "xxx.xxx.xxx.com"
                },
              ],
              "deploy_mode": "local",
              "maven_params": " -DskipTests=true",
              "shared_assets": [
                {
                  "from": "logs",
                  "to": "/home/${user}/app-data/logs/deploy-test",
                  "chmod": "755"
                },
                {
                  "from": "data",
                  "to": "/home/${user}/app-data/data/deploy-test",
                  "chmod": "755"
                }
              ]
            }

            

```
