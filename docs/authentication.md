# Authentication

## SSH Authentication
SSH Authentication Support Key And Password
1. set key config in deploy.json
    1. keyfile
    1. keyfilepass 可选 如果sshkey没有密码可以用不用填
## SSH 密码认证
1. user
1. password

## SCM Authentication
### Scm Authentication By Deploy Key
1. How to set Deploy Key
    1. Github
        1. Settings -> Deploy keys -> Add deploy key
    1. Gitlab
        1. gitlab -> Deploy Keys -> Key: Input Deploy Pubkey 
        1. 第二次 其他项目可以enable 公用 deploy key
1. set config in deploy.json
    1. scm_keyfile
    1. scm_keyfilepass 可选
    
### SCM 密码认证
1. set scm user password in deploy.json
    1. scm_username
    1. scm_password



## SSH KEY && SCM KEY
1. 默认ssh key 和 scm key 一样
2. 如果scm key 不一样 需要单独设置scm_keyfile
3. 如需要为你的项目创建单独deploy scm key 可以通过ssh-keygen 生成一个单独的ssh key(注意文件位置不要把本身用户ssh key 误覆盖了)
4. 通过添加.ssh/config 设置指定host使用不同的key 如下 

    ```
       Host git.xxx.com
         HostName git.xxx.com
         IdentitiesOnly yes
         IdentityFile ~/.ssh/id_rsa_xxx 
    ```
5. 设置配置文件权限 chmod 600  ~/.ssh/config
