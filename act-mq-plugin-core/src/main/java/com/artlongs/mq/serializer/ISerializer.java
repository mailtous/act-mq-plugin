package com.artlongs.mq.serializer;

import java.io.Serializable;

public interface ISerializer {

    public byte[] toByte(Object obj);

    public <T extends Serializable> T getObj(byte[] bytes);
    public <T extends Serializable> T getObj(byte[] bytes,Class<T> clzz);
}
