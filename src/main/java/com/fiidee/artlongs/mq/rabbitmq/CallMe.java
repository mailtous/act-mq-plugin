package com.fiidee.artlongs.mq.rabbitmq;

/**
 * 接收到消息后,做你想做的事:),类似与JS的回调方法
 * Created by leeton on 8/25/17.
 */
@FunctionalInterface
public interface CallMe<T> {
    abstract void exec(T t);
}
