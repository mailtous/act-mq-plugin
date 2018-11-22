package com.fiidee.artlongs.mq.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.fiidee.artlongs.mq.MqEntity;
import com.fiidee.artlongs.mq.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Kryo序列化
 * Created by leeton on 8/29/17.
 */
public class KryoSerializer implements ISerializer {

    private Kryo kryo = null;

    public KryoSerializer(){
        kryo = KryoHolder.get();
    }

    public byte[] getByte(Object object) {
        kryo.register(object.getClass(), new JavaSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        byte[] b = baos.toByteArray();
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    public <T extends Serializable> T getObj(byte[] bytes) {
        Input input = new Input(new ByteArrayInputStream(bytes));
        Class<T> clzz = kryo.readClass(input).getType();
        T obj = kryo.readObject(input, clzz);
        return obj;

    }

    @Override
    public <T extends Serializable> T getObj(byte[] bytes, Class<T> clzz) {
        Input input = new Input(bytes);
        T obj = kryo.readObject(input, clzz);
        return obj ;
    }

    public static void main(String[] args) {
        MqEntity msgEntity = MqEntity.ofDef("test");

        byte[] bytes = new KryoSerializer().getByte(msgEntity);
        MqEntity entity =  new KryoSerializer().getObj(bytes);
        System.out.println("entity = [" + entity + "]");

    }
}
