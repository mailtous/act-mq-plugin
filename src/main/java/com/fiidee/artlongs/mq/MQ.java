package com.fiidee.artlongs.mq;


import act.event.EventBus;
import com.fiidee.artlongs.mq.rabbitmq.CallMe;
import com.fiidee.artlongs.mq.serializer.ISerializer;

/**
 * 消息的发送与接收
 * Created by leeton on 8/18/17.
 */
public interface MQ {

    /**
     * 发布消息,使用默认的转换器及队列
     * @param msg
     * @param topic
     * @param type
     * @param <MODEL>
     * @return
     */
    abstract <MODEL> MsgEntity send(MODEL msg, String topic , SendType type);

    /**
     * 发送消息
     * @param msg   消息实体
     * @param exchangeName 转换器
     * @param queueName    队列
     * @param topic        消息KEY
     * @param type         消息传播方式
     * @param <MODEL>
     * @return
     */
    abstract <MODEL> MsgEntity send(MODEL msg, String exchangeName, String queueName, String topic , SendType type);

    /**
     * 创建消费者并监听消息,收到消息后回调要执行的方法
     * @param exchangeName 转换器
     * @param queueName    消息队列
     * @param channel      消息通道
     * @param CallMe 回调(做你想做的事)
     * @return
     */
    abstract boolean receive(String exchangeName, String queueName, String topic,CallMe callMe);

    /**
     * (推荐)创建消费者并监听消息,收到消息后再发布内部事件
     * @param exchangeName 转换器
     * @param queueName    消息队列
     * @param channel      消息通道
     * @param eventKey     内部事件KEY,使用@On("enventKey"),去监听事件
     * @return
     */
    abstract boolean receive(String exchangeName, String queueName, String topic,String eventKey);

    /**
     * 初始化消息服务器
     * @param config
     * @param eventBus
     * @param serializer
     * @return
     */
    abstract MQ init(MqConfig config , EventBus eventBus, ISerializer serializer);


    /**
     * 消息传播方式
     */
    enum SendType{
        FANOUT,  //广播的方式发布消息
        TOPIC;   //主题匹配的方式发布消息
    }


}
