# community
基于springboot的论坛项目

kafka启动命令：

# 存放路径（看自己情况）
E:\javaweb\work\kafka_2.12-2.8.0
# windows运行路径（看自己的情况）
E:\javaweb\work\kafka_2.12-2.8.0\bin\windows
# 启动服务器  （先启动zookeeper服务器，再启动kafka）  ！！！千万不要手动暴力关闭，用下面的命令关闭
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
bin\windows\kafka-server-start.bat config\server.properties
# 创建主题
kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1  --topic test
# 查看当前服务器的主题
kafka-topics.bat --list --bootstrap-server localhost:9092
# 创建生产者，往指定主题上发消息
kafka-console-producer.bat --broker-list localhost:9092 --topic test
# 消费者
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
# 关闭zookeeper服务器
zookeeper-server-stop.bat
# 关闭kafka服务器
kafka-server-stop.bat


#命令备忘

# kafka
# 进入kafka主目录
cd D:\develop\kafka_2.12-2.3.0\bin\windows
# 根据配置文件启动zookeeper
.\zookeeper-server-start.bat config\zookeeper.properties
# 根据配置文件启动kafka
.\kafka-server-start.bat config\server.properties

# Elasticsearch
# 进入Elasticsearch主目录
cd D:\develop\elasticsearch-6.4.3\bin
# 启动Elasticsearch
.\elasticsearch.bat