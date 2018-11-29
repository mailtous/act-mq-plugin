package com.artlongs.act.mq.plugin.core.serializer;

import com.artlongs.act.mq.plugin.core.serializer.kryo.KryoSerializer;
import com.artlongs.act.mq.plugin.core.MqConfig;

import java.io.Serializable;

public interface ISerializer {

    public byte[] toByte(Object obj);
    public <T extends Serializable> T getObj(byte[] bytes);
    public <T extends Serializable> T getObj(byte[] bytes,Class<T> clzz);


    enum Serializer{
        INST;
        public ISerializer of(){
            switch (MqConfig.serializer.get()) {
                case MqConfig.serializer_fst:
                    return new Fst2Serializer();
                case MqConfig.serializer_kryo:
                    return new KryoSerializer();
                case MqConfig.serializer_fastjson:
                    return new FastJsonSerializer();
                default:
                    return new FastJsonSerializer();
            }
        }
    }
}
