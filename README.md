# analyze
采用了jstorm、rocketmq、hbase的一个从rocketmq获取数据，持久化到hbase的样例程序。
##改动历史
2018.04.24 优化hbase的存储效率

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

# 注意
在使用rocketmq作为spout数据源的时候，需要注意的是如果配置spout的并行数最好为1，然后topology的workderNum为多个。

# 提交jstorm
$JSTORM_HOME/bin/jstorm jar analyze-1.0-SNAPSHOT.jar com.lehoon.analyze.jstorm.topology.AnalyzeTopology analyze.yaml