package com.fiidee.artlongs.mq;

import act.event.EventBus;
import com.fiidee.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.fiidee.artlongs.mq.redis.RedisMqImpl;
import com.fiidee.artlongs.mq.rocketmq.RocketMqImpl;
import com.fiidee.artlongs.mq.serializer.*;
import com.fiidee.artlongs.mq.serializer.kryo.KryoSerializer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MqManager {

    private static MQ mq;
    private static MqManager manager;
    private static MqConfig mqConfig;

    @Inject
    private static EventBus eventBus;

    private MqManager() {
    }

    public static MqManager me() {
        if (manager == null) {
            manager = new MqManager();
            if (null == mqConfig) {
                mqConfig = new MqConfig();
            }
        }
        return manager;
    }

    public MQ getMq() {
        if (mq == null) {
            mq = buildMq();
        }
        return mq;
    }

    private MQ buildMq() {
        switch (mqConfig.provider.get()) {
            case MqConfig.provider_redis:
                return getRedisMq();
            case MqConfig.provider_rabbitmq:
                return new RabbitMqImpl().init(mqConfig,eventBus,getserializer());
            case MqConfig.provider_rocketmq:
                return new RocketMqImpl().init(mqConfig,eventBus,getserializer());
            case MqConfig.provider_activemq:
            case MqConfig.provider_zmq:
                throw new RuntimeException("TODO NEW MQ ...");
            default:
                return getRedisMq();
        }
    }

    private MQ getRedisMq(){
        if (null == mq) {
            mq = new RedisMqImpl().init(mqConfig,eventBus,getserializer());
        }
        return mq;
    }

    private ISerializer getserializer(){
        switch (mqConfig.serializer.get()) {
            case MqConfig.serializer_fst:
                return new Fst2Serializer();
            case MqConfig.serializer_kryo:
                return new KryoSerializer();
            case MqConfig.serializer_fastjson:
                return new FastJsonSerializer();
            case MqConfig.serializer_codejson:
                return new CodeJson();
            case MqConfig.serializer_gson:
                return new GsonSerializer();
            default:
                return new FastJsonSerializer();
        }
    }


}
