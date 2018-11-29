package com.artlongs.act.mq.plugin.core.serializer;

import com.alibaba.fastjson.JSON;
import com.artlongs.act.mq.plugin.core.MqEntity;

import java.io.Serializable;

/**
 * Created by leeton on 8/31/17.
 */
public class FastJsonSerializer implements ISerializer {
    @Override
    public byte[] toByte(Object obj) {

        return JSON.toJSONBytes(obj);
    }


    @Override
    public <T extends Serializable>  T getObj(byte[] bytes) {
        T obj = JSON.parseObject(bytes,MqEntity.class);
        return obj;
    }


    @Override
    public <T extends Serializable>  T getObj(byte[] bytes, Class<T> clzz) {
        T obj = JSON.parseObject(bytes, clzz);
        return obj;
    }


    public static void main(String[] args) {
        MqEntity msgEntity = MqEntity.ofDef("test");
        byte[] bytes = new FastJsonSerializer().toByte(msgEntity);
        MqEntity entity =  new FastJsonSerializer().getObj(bytes);
        System.out.println("entity = [" + entity + "]");
    }

}
