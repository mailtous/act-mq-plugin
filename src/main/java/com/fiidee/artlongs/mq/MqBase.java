package com.fiidee.artlongs.mq;

import act.event.EventBus;
import com.fiidee.artlongs.mq.rabbitmq.CallMe;
import com.fiidee.artlongs.mq.serializer.ISerializer;

import javax.inject.Singleton;

/**
 * Created by leeton on 8/22/17.
 */
@Singleton
public abstract class MqBase implements MQ {

    protected static MqConfig mqConfig;
    public abstract <MODEL> MsgEntity send(MODEL msg, String topic , SendType sendType);
    public abstract <MODEL> MsgEntity send(MODEL msg, String exchangeName, String queueName, String topic , SendType sendType);
    public abstract boolean receive(String exchangeName, String queueName, String topic,CallMe callMe);
    public abstract boolean receive(String exchangeName, String queueName, String topic,String eventKey);

    public abstract MQ init(MqConfig config , EventBus eventBus, ISerializer serializer);
}
