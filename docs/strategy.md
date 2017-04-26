# 部署策略 strategy

* maven 类型
    * maven:assembly 使用assembly:assembly 打包
    * maven:install 执行mvn install
    * maven:package 使用 package 打包 (如果使用shade 插件打成依赖包也用这个)
    * maven:assembly:archive   (使用assembly:single 使用assembly.xml 方式 生成 tar.gz 方式)
* script类型
    * code 脚本语言以及非编译类语言
