package com.fiidee.artlongs.mq.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

/**
 * Act 编译匿名类会报错,没办法,只好自定义一个接口通过JDK8 lamber写法来进行消息的监听。
 * Created by leeton on 8/23/17.
 */
@FunctionalInterface
public interface Receiver extends Consumer {

    @Override
    default void handleConsumeOk(String consumerTag) {

    }

    @Override
    default void handleCancelOk(String consumerTag) {

    }

    @Override
    default void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    default void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

    }

    @Override
    default void handleRecoverOk(String consumerTag) {

    }

    /**
     * No-op implementation of {@link com.rabbitmq.client.Consumer#handleDelivery}.
     */
    @Override
    public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException;

}
