# Authentication

# SSH Authentication
# SCM Authentication



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
