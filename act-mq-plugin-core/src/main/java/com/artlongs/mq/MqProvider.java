package com.artlongs.mq;

import com.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.artlongs.mq.redis.RedisMqImpl;
import com.artlongs.mq.rocketmq.RocketMqImpl;
import com.artlongs.mq.serializer.*;
import com.artlongs.mq.serializer.kryo.KryoSerializer;

import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class MqProvider implements Provider<MQ> {

    @Override
    public MQ get() {
        return buildMq();
    }

    private MQ buildMq() {
        switch (MqConfig.provider.get()) {
            case MqConfig.provider_redis:
                return getRedisMq();
            case MqConfig.provider_rabbitmq:
                return new RabbitMqImpl().init(getSerializer());
            case MqConfig.provider_rocketmq:
                return new RocketMqImpl().init(getSerializer());
            case MqConfig.provider_activemq:
                throw new RuntimeException("TODO activemq mq provider ...");
            case MqConfig.provider_zmq:
                throw new RuntimeException("TODO zmq mq provider ...");
            default:
                return getRedisMq();
        }
    }

    private MQ getRedisMq(){
       return new RedisMqImpl().init(getSerializer());
    }

    private ISerializer getSerializer(){
        switch (MqConfig.serializer.get()) {
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
