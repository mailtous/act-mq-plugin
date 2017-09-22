package com.fiidee.artlongs.mq.serializer;

import java.io.Serializable;

public interface ISerializer {

    public byte[] getByte(Object obj);

    public <T extends Serializable> T getObj(byte[] bytes);
    public <T extends Serializable> T getObj(byte[] bytes,Class<T> clzz);
}
