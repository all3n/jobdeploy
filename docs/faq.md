# 常见问题
1. 无法创建文件夹
	* 使用不同账户，执行执行脚本时候sudo经常会碰到 sudo: sorry, you must have a tty to run sudo这个情况，其实修改一下sudo的配置就好了
	* vi /etc/sudoers 
	* 注释掉 Default requiretty 一行
	* 意思就是sudo默认需要tty终端。注释掉就可以在后台执行了。

2. 无法使用sshkey
	* .ssh 权限
	* authorized_keys 600
	* .ssh 700

3. Permission denied
	* 如果使用账号不具有创建目标部署文件夹目录权限导致
	* 如果你部署user 为 deploy 你要部署到 /target/a1
	* 可以通过 chgrp deploy /target,chmod 775 /target 修改目标目录上层目录group 权限
