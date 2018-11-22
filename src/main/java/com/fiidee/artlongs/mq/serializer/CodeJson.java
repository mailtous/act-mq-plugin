package com.fiidee.artlongs.mq.serializer;

import com.fiidee.artlongs.mq.MqEntity;
import link.jfire.codejson.JsonTool;

import java.io.Serializable;

/**
 * Created by leeton on 8/31/17.
 */
public class CodeJson implements ISerializer {
    @Override
    public byte[] getByte(Object obj) {
        String jsonStr = JsonTool.write(obj);
        return jsonStr.getBytes();
    }

    @Override
    public <T extends Serializable> T getObj(byte[] bytes) {
        String jsonStr = new String(bytes);
        T obj = JsonTool.read(MqEntity.class, jsonStr);
        return obj;
    }


    @Override
    public <T extends Serializable> T getObj(byte[] bytes, Class<T> clzz) {
        String jsonStr = new String(bytes);
        T obj = JsonTool.read(clzz, jsonStr);
        return obj;
    }

    public static void main(String[] args) {
        MqEntity msgEntity = MqEntity.ofDef("test");
        byte[] bytes = new CodeJson().getByte(msgEntity);
        MqEntity entity =  new CodeJson().getObj(bytes);
        System.out.println("entity = [" + entity + "]");
    }

}
