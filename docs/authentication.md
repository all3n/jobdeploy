# Authentication

## SSH Authentication
SSH Authentication Support Key And Password
1. set key config in deploy.json
    1. keyfile
    1. keyfilepass is an optional parameter that you can use when configuring SSH keys. It is used when your SSH key is password-protected.
## SSH Password Authentication
1. user
1. password

## SCM Authentication
### Scm Authentication By Deploy Key
1. How to set Deploy Key
    1. Github
        1. Settings -> Deploy keys -> Add deploy key
    1. Gitlab
        1. gitlab -> Deploy Keys -> Key: Input Deploy Pubkey 
        1. For the second deployment and other projects, you can enable the use of a shared deploy key.
1. set config in deploy.json
    1. scm_keyfile
    1. scm_keyfilepass Optional
    
### SCM Password Authentication
1. set scm user password in deploy.json
    1. scm_username
    1. scm_password


## SSH KEY && SCM KEY
1. The default SSH key and SCM key are the same.
2. If the SCM key is different, you need to set the `scm_keyfile` separately.
3. If you need to create a separate deploy SCM key for your project, you can use `ssh-keygen` to generate a separate SSH key (be careful not to overwrite your existing user SSH key by mistake).
4. You can set different keys for specific hosts by adding the following configuration to your `.ssh/config` file:

    ```
    Host git.xxx.com
        HostName git.xxx.com
        IdentitiesOnly yes
        IdentityFile ~/.ssh/id_rsa_xxx
    ```

5. Set the permissions of the configuration file using `chmod 600 ~/.ssh/config`.
