package com.fiidee.artlongs.mq.rocketmq;

import act.event.EventBus;
import com.fiidee.artlongs.mq.MQ;
import com.fiidee.artlongs.mq.MqConfig;
import com.fiidee.artlongs.mq.MsgEntity;
import com.fiidee.artlongs.mq.rabbitmq.CallMe;
import com.fiidee.artlongs.mq.serializer.ISerializer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.osgl.logging.L;
import org.osgl.logging.Logger;

/**
 * ROCKETMQ 的消息发送与接收
 *
 * Created by leeton on 8/22/17.
 */
public class RocketMqImpl implements MQ {
    private static Logger logger = L.get(RocketMqImpl.class);

    private static EventBus eventBus;
    private static ISerializer serializer;
    private DefaultMQProducer producer = buildProducer();

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String topic, SendType sendType) {
        MsgEntity msgEntity = new MsgEntity();
        if (producer != null) {
            try {

                msgEntity.setMsg(msg);
                Message message = new Message();
                message.setTopic(topic);
                message.setTags("");
                message.setBody(serializer.getByte(msgEntity));
                SendResult sendResult = producer.send(message);
                if (sendResult != null && SendStatus.SEND_OK == sendResult.getSendStatus()) {
                    logger.debug("[SEND] msg = [" + msg + "], topic = [" + topic + "], sendType = [" + sendType + "]");
                    msgEntity.setSended(true);
                }
                return msgEntity;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //producer.shutdown();
            }

        }
        return msgEntity;
    }

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String exchangeName, String queueName, String topic, SendType sendType) {
        return send(msg, topic, sendType);
    }


    @Override
    public boolean subscribe(String exchangeName, String queueName, String topic, CallMe callMe) {
       return pullMessageAndDoJob(topic, callMe, "");
    }

    @Override
    public boolean subscribe(String exchangeName, String queueName, String topic, String eventKey) {
        return pullMessageAndDoJob(topic, null, eventKey);
    }

    /**
     * PULL服务端MQ的消息,并执行真正的业务逻辑
     * @param topic 消息主题
     * @param callMe   回调方法
     * @param eventKey 回调的事件KEY
     * @return
     */
    private boolean pullMessageAndDoJob(String topic, CallMe callMe, String eventKey) {
        DefaultMQPushConsumer consumer = buildConsumer();

        if (consumer != null) {
            try {
                consumer.subscribe(topic, "*");
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                MessageListenerConcurrently listener = (msgList, context) -> {
                    logger.debug("[RECV]" + Thread.currentThread().getName() + " Receive New Messages: " + msgList.size());
                    if (msgList.size() > 0) {
                        MessageExt msg = msgList.get(0);
                        MsgEntity msgEntity = serializer.getObj(msg.getBody(), MsgEntity.class);
                        //执行真正的业务
                        if (callMe != null) {
                            callMe.exec(msgEntity);
                        } else {
                            eventBus.trigger(eventKey, msgEntity);
                        }
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                };
                consumer.registerMessageListener(listener);
                consumer.start();
                return true;
            } catch (MQClientException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    @Override
    public RocketMqImpl init(EventBus eventBus, ISerializer serializer) {
        this.eventBus = eventBus;
        this.serializer = serializer;
        return this;
    }


    private static DefaultMQProducer buildProducer() {
        String groupName = MqConfig.rocketmq_producergroupname.get() + System.currentTimeMillis() + "";
        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(MqConfig.rocketmq_namesrvaddr.get());
        producer.setInstanceName(MqConfig.rocketmq_producer.get());
        producer.setVipChannelEnabled(false);
        try {
            producer.start();
        } catch (MQClientException e) {
            producer.shutdown();
            e.printStackTrace();
        }
        return producer;
    }


    /**
     * 创建消息者
     * @return
     */
    private DefaultMQPushConsumer buildConsumer() {
        String groupName = MqConfig.rocketmq_consumergroupname.get() + System.currentTimeMillis() + "";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);  //实质上还是拉取消息,无语
        consumer.setNamesrvAddr(MqConfig.rocketmq_namesrvaddr.get());
        consumer.setInstanceName(MqConfig.rocketmq_consumer.get());
        consumer.setVipChannelEnabled(false);
        return consumer;
    }

    public static CallMe toShow() {
        CallMe todo = (c) -> {
            System.err.println("[CALLME]: " + c);
        };
        return todo;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
