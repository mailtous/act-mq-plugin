package com.fiidee.artlongs.mq;

import act.event.EventBus;
import com.fiidee.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.fiidee.artlongs.mq.rocketmq.RocketMqImpl;

import javax.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static act.Act.app;

/**
 * MQ消息接收器
 * Created by leeton on 9/22/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MqReceiver {
    String value();                 // TOPIC,消息主题(通用)
    String exchange() default "";   // 交换器,RABBITMQ才会使用到
    String queue() default "";      // 队列

    MQ mq = MqManager.me().getMq();  //MQ 实例

    class Listener{
        //TODO:收到MQ消息后,把消息内容转给加了本注解的具体方法
        boolean isReceived = mq.receive(RabbitMqImpl.default_exchange,RabbitMqImpl.default_queue,"topic", RabbitMqImpl.toShow());
    }

}
