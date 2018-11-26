package com.artlongs.mq.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * Created by leeton on 8/30/17.
 */
public class KryoHolder {
/*
    private static ThreadLocal<Kryo> threadLocalKryo = new ThreadLocal<Kryo>()
    {
        protected Kryo initialValue()
        {
            Kryo kryo = new KryoReflectionFactory();

            return kryo;
        };
    };
*/


   private static ThreadLocal<Kryo> threadLocalKryo = new MyTheradLocal<Kryo>();

    public static Kryo get()
    {
        return threadLocalKryo.get();
    }

    public static class MyTheradLocal<T> extends ThreadLocal{

        public MyTheradLocal(){
            initialValue();
        }
        @Override
        public Kryo initialValue() {
            Kryo kryo = new KryoReflectionFactory();
            return kryo;
        };
    }
}
