package com.fiidee.artlongs.mq.redis;

import act.event.EventBus;
import com.fiidee.artlongs.mq.MQ;
import com.fiidee.artlongs.mq.MqBase;
import com.fiidee.artlongs.mq.MqConfig;
import com.fiidee.artlongs.mq.MsgEntity;
import com.fiidee.artlongs.mq.rabbitmq.CallMe;
import com.fiidee.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.fiidee.artlongs.mq.serializer.ISerializer;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.Charset;

/**
 * REDIS 消息发送与接收
 * Created by leeton on 8/22/17.
 */
public class RedisMqImpl extends MqBase implements MQ {
    private static Logger logger = L.get(RedisMqImpl.class);

    private EventBus eventBus;
    private ISerializer serializer;
    private JedisPool jedisPool;

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String topic, SendType sendType) {
        MsgEntity msgEntity = new MsgEntity();
        byte[] redisKey = topic.toString().getBytes(Charset.forName("utf-8"));
        byte[] message = serializer.getByte(msgEntity.setMsg(msg));
        msgEntity.setSended(publish(redisKey, message));
        return msgEntity;
    }

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String exchangeName, String queueName, String topic, SendType sendType) {
        return send(msg, topic, sendType);
    }

    @Override
    public boolean receive(String exchangeName, String queueName, String topic,CallMe todo) {
        byte[] key = topic.toString().getBytes();
        return receive(key, todo,"");
    }

    @Override
    public boolean receive(String exchangeName, String queueName, String topic, String eventKey) {
        byte[] key = topic.toString().getBytes();
        return receive(key, null,eventKey);
    }

    @Override
    public RedisMqImpl init(MqConfig config, EventBus eventBus, ISerializer serializer) {
        this.jedisPool = RedisConfig.buildConnetion(config);
        this.eventBus = eventBus;
        this.serializer = serializer;
        return this;
    }


    public Jedis getJedis(){
        return jedisPool.getResource();
    }

    /**
     * 发布
     *
     * @param key
     * @param message
     */
    private boolean publish(byte[] key, byte[] message) {
        Jedis jedis = getJedis();
        try {
            Long rt = getJedis().publish(key, message);
            logger.debug("msg is sended ,recever count: = " +rt);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return false;
    }

    /**
     * 接收消息
     * @param key  消息TOPIC
     * @param callMe   回调方法(可选)
     * @param eventKey 回调的事件(可选)
     * @return
     */
    private boolean receive(byte[] key,CallMe callMe,String eventKey){
       Jedis jedis = getJedis();
        RedisSubsribe subsribe = new RedisSubsribe(jedis,key,serializer,callMe,eventBus,eventKey);
        new Thread(subsribe).start();
        return true;
    }


}
