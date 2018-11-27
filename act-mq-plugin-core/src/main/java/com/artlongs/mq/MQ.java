package com.artlongs.mq;


import com.artlongs.mq.serializer.ISerializer;
import com.artlongs.mq.rabbitmq.CallMe;

import javax.inject.Singleton;

/**
 * 消息的发送与接收
 * Created by leeton on 8/18/17.
 */
public interface MQ {

    /**
     * 消息传播方式
     */
    enum Spread {
        FANOUT,  //广播的方式发布消息
        TOPIC   //主题匹配的方式发布消息
    }

    class Module extends org.osgl.inject.Module {
        @Override
        protected void configure() {
            bind(MQ.class).in(Singleton.class).to(new MqProvider());
        }
    }

    /**
     * 初始化消息服务器
     * @param serializer
     * @return
     */
    MQ init(ISerializer serializer);

    /**
     * 发布消息 (消息传播方式默认为 TOPIC)
     * @param mqEntity
     * @param <MODEL>
     * @return
     */
    <MODEL> MqEntity send(MqEntity mqEntity);

    /**
     * 发布消息
     * @param mqEntity
     * @param spread
     * @param <MODEL>
     * @return
     */
    <MODEL> MqEntity send(MqEntity mqEntity, Spread spread);

    /**
     * (推荐)创建消费者并监听消息,收到消息后再发布内部事件
     * @param eventKey     内部事件KEY,使用@On("enventKey"),去监听事件
     * @return
     */
    boolean subscribe(MqEntity.Key key, String eventKey);


    /**
     * 创建消费者并监听消息,收到消息后回调要执行的方法
     * @param callMe 回调(做你想做的事)
     * @return
     */
    boolean subscribe(MqEntity.Key key, CallMe callMe);






}
