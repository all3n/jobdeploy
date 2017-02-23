# 编译使用
* 安装
    * one key install:
    *   curl https://raw.githubusercontent.com/all3n/jobdeploy/master/install -L -o - |sh
    * OR you cal install by src:
    * git clone https://github.com/all3n/jobdeploy.git
    * cd jobdeploy
    * ./install
    
* 注意：    
    * 默认 install /opt/jobdeploy 下,如果要安装其他 目录 ./install /path/to
    * 安装之后jar包放在输入位置，script会软连接 deploy 放在/usr/bin/deploy下
* 安装后更新
    * deploy --update 
