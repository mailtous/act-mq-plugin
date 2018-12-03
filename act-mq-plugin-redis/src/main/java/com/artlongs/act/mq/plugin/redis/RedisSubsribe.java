package com.artlongs.act.mq.plugin.redis;

import act.event.EventBus;
import com.artlongs.act.mq.plugin.core.MQ;
import com.artlongs.act.mq.plugin.core.MqEntity;
import com.artlongs.act.mq.plugin.core.CallMe;
import com.artlongs.act.mq.plugin.core.serializer.ISerializer;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 * REDIS 消息订阅器
 * Created by leeton on 9/13/17.
 */
public class RedisSubsribe extends BinaryJedisPubSub implements Runnable {

    private final Jedis jedis;
    private byte[] topic;
    private ISerializer serializer;
    private CallMe callMe = null;
    private EventBus eventBus = null;
    private String eventKey = "";

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        exec(message);
        unsubscribe(channel);
    }

    /**
     * 创建订阅器及执行回调
     * @param jedis   REDIS
     * @param topic   订阅的主题
     * @param serializer 消息的序列化器
     * @param callMe     回调方法
     * @param eventBus   回调的事件
     * @param eventKey   回调的事件KEY
     */
    public RedisSubsribe(Jedis jedis, byte[] topic, ISerializer serializer, CallMe callMe, EventBus eventBus, String eventKey) {
        this.callMe = callMe;
        this.eventBus = eventBus;
        this.eventKey = eventKey;
        this.jedis = jedis;
        this.topic = topic;
        this.serializer = serializer;
    }

    private void exec(byte[] message) {
        MqEntity mqEntity = serializer.getObj(message);
        mqEntity.setReaded(true);
        mqEntity.setSended(true);
        if (null !=mqEntity && null != callMe) {
            callMe.exec(mqEntity);
        } else {
            eventBus.triggerAsync(eventKey, mqEntity);
        }

    }

    @Override
    public void run() {
        jedis.subscribe(this, topic); //jedis.subscribe() 是单线程阻塞式的,所以要新开线程来执行
    }
}
