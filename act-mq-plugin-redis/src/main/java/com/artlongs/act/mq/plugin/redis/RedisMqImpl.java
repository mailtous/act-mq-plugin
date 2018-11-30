package com.artlongs.act.mq.plugin.redis;

import act.app.App;
import act.event.EventBus;
import com.artlongs.act.mq.plugin.core.*;
import com.artlongs.act.mq.plugin.core.annotation.RedisMq;
import com.artlongs.act.mq.plugin.core.serializer.ISerializer;
import org.osgl.inject.annotation.Provides;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.Charset;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * REDIS 消息发送与接收
 * Created by leeton on 8/22/17.
 */
@Singleton
public class RedisMqImpl implements MQ {
    private static Logger logger = L.get(RedisMqImpl.class);

    private EventBus eventBus;
    private ISerializer serializer;
    private JedisPool jedisPool;

    public static class Module extends org.osgl.inject.Module {
        @Override
        protected void configure() {
            bind(MQ.class).in(Singleton.class).qualifiedWith(RedisMq.class).named("redismq").to(new Provider<MQ>() {
                @Override
                public MQ get() {
                    return new RedisMqImpl();
                }
            });
        }
    }


    public RedisMqImpl() {
        init(ISerializer.Serializer.INST.of());
    }

    @Override
    @Provides
    public RedisMqImpl init(ISerializer serializer) {
        this.jedisPool = RedisConfig.buildConnetion();
        this.serializer = serializer;
        this.eventBus = App.instance().eventBus();
        return this;
    }



    //返回 REDIS 数据源
    public Jedis getJedis(){
        return jedisPool.getResource();
    }


    @Override
    public <MODEL> MqEntity send(MqEntity mqEntity) {
        byte[] redisKey = mqEntity.getKey().getTopic().getBytes(Charset.forName("utf-8"));
        byte[] message = serializer.toByte(mqEntity);
        mqEntity.setSended(publish(redisKey, message));
        return mqEntity;
    }

    @Override
    public <MODEL> MqEntity send(MqEntity mqEntity, Spread sendType) {
        mqEntity.setSpread(sendType);
        return send(mqEntity);
    }


    @Override
    public boolean subscribe(MqEntity.Key key, CallMe todo) {
        byte[] redisKey = key.getTopic().getBytes();
        return subscribe(redisKey, todo,"");
    }

    @Override
    public boolean subscribe(MqEntity.Key key, String eventKey) {
        byte[] redisKey = key.getTopic().getBytes();
        return subscribe(redisKey, null,eventKey);
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
            return rt>0;
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
    private boolean subscribe(byte[] key, CallMe callMe, String eventKey){
       Jedis jedis = getJedis();
        RedisSubsribe subsribe = new RedisSubsribe(jedis,key,serializer,callMe,eventBus,eventKey);
        new Thread(subsribe).start();
        return true;
    }


}
