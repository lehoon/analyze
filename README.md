# analyze
采用了jstorm、rocketmq、hbase的一个从rocketmq获取数据，持久化到hbase的样例程序。

# 说明文档
[文档地址](http://blog.lehoon.cn/backend/2017/06/13/jstorm-hbase-issue.html)


# 运行环境
Jstorm 2.2.1

RocketMQ 3.5.8

Hbase 1.2.2

Jdk 1.8


# 编译命令
打包工程依赖文件到jar包命令。

mvn package assembly:single

# 提交jstorm
$JSTORM_HOME/bin/jstorm jar analyze-1.0-SNAPSHOT.jar com.lehoon.analyze.jstorm.topology.AnalyzeTopology analyze.yaml