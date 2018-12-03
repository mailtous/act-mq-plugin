# Act-Mq-Plugin
使用ACT做的一个MQ 小插件，目前包括redis,rocketmq,rabbitmq(推荐) 三种MQ消息的接收与发送
开发及运行环境 jdk8,idea,redis(rabbitmq,rocketmq)

# 项目启动
act-mq-plugin-tester --> AppStart
项目的启动目录要配置成:%MODULE_WORKING_DIR%


# 使用方法

通常一个系统只使用一种 MQ , 故按需引入要使用的 MQ 依赖
EG:使用 REDIS-MQ,则只需要引入:
```xml
        <dependency>
            <groupId>act-mq-plugin</groupId>
            <artifactId>act-mq-plugin-redis</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

```
对应的 mq.properties 只需配置好 redis 属性项即可:
``` properties
#==== 使用那种序列化(kryo,fst,fastjson,gson) ====
mq.serializer=fastjson
#==== redis mq setting ====
mq.redis.host=127.0.0.1
mq.redis.port=6379
mq.redis.connetion.timeout=20000
mq.redis.database=0
mq.redis.username=xxx
mq.redis.password=xxx

```

 客户端 APP 调用时,注入相应的 MQ 服务,EG:
``` java
    @RedisMq
    private MQ redismq;

    @RabbitMq
    private MQ rabbitmq;

    @RocketMq
    private MQ rocketmq;

```

#示例项目
参见 act-mq-plugin-tester
