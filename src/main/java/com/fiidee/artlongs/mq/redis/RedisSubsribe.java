package com.fiidee.artlongs.mq.redis;

import act.event.EventBus;
import com.fiidee.artlongs.mq.MqEntity;
import com.fiidee.artlongs.mq.rabbitmq.CallMe;
import com.fiidee.artlongs.mq.serializer.ISerializer;
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
        subscribe(channel);//释放订阅
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
        MqEntity msg = serializer.getObj(message);
        if (null != callMe) {
            callMe.exec(msg);
        } else {
            eventBus.trigger(eventKey, msg);
        }
    }

    @Override
    public void run() {
        jedis.subscribe(this, topic); //jedis.subscribe() 是单线程阻塞式的,所以要新开线程来执行
    }
}
