package com.fiidee.artlongs.mq.serializer;


import com.fiidee.artlongs.mq.MsgEntity;
import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;


public class Fst2Serializer implements ISerializer {

    static FSTConfiguration fst = FSTConfiguration.createDefaultConfiguration();

    public byte[] getByte(Object obj) {
        if (obj == null) return null;
        return fst.asByteArray(obj);
    }

    public <T extends Serializable> T getObj(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        T obj = (T) fst.asObject(bytes);
        return obj;
    }

    @Override
    public <T extends Serializable> T getObj(byte[] bytes, Class<T> clzz) {
        return getObj(bytes);
    }

    public static void main(String[] args) {
        MsgEntity msgEntity = new MsgEntity();
        msgEntity.setMsg("test");

        byte[] bytes = new Fst2Serializer().getByte(msgEntity);
        MsgEntity entity =  new Fst2Serializer().getObj(bytes);
        System.out.println("entity = [" + entity + "]");

    }

}
