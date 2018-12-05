package com.artlongs.act.mq.plugin.rocket;

import act.app.App;
import act.app.conf.AppConfigPlugin;
import act.event.EventBus;
import com.artlongs.act.mq.plugin.core.CallMe;
import com.artlongs.act.mq.plugin.core.MQ;
import com.artlongs.act.mq.plugin.core.MqConfig;
import com.artlongs.act.mq.plugin.core.MqEntity;
import com.artlongs.act.mq.plugin.core.annotation.RocketMq;
import com.artlongs.act.mq.plugin.core.serializer.ISerializer;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
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

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * ROCKETMQ 的消息发送与接收
 * <p>
 * Created by leeton on 8/22/17.
 */
@Singleton
@Named("rocketmq")
public class RocketMqImpl implements MQ {
    private static Logger logger = L.get(RocketMqImpl.class);

    private EventBus eventBus;
    private static ISerializer serializer;
    private DefaultMQProducer producer;

    public RocketMqImpl() {
        init(ISerializer.Serializer.INST.of());
    }

    @Override
    public RocketMqImpl init(ISerializer serializer) {
        this.serializer = serializer;
        this.eventBus = App.instance().eventBus();
        return this;
    }

    public static class Module extends org.osgl.inject.Module {
        @Override
        protected void configure() {
            bind(MQ.class).in(Singleton.class).qualifiedWith(RocketMq.class).named("rocketmq").to(new Provider<MQ>() {
                @Override
                public MQ get() {
                    return new RocketMqImpl();
                }
            });
        }
    }


    @Override
    public <MODEL> MqEntity send(MqEntity mqEntity) {

        return send(mqEntity, mqEntity.getSpread());
    }

    @Override
    public <MODEL> MqEntity send(MqEntity mqEntity, Spread sendType) {
        if (buildProducer() != null) {
            try {
                Message message = new Message();
                message.setBuyerId(mqEntity.getId());
                message.setTopic(mqEntity.getKey().getTopic());
                message.setTags(mqEntity.getKey().getTags());
                message.setBody(serializer.toByte(mqEntity));
                SendResult sendResult = producer.send(message);
                if (sendResult != null && SendStatus.SEND_OK == sendResult.getSendStatus()) {
                    mqEntity.setSended(true);
                    logger.debug("[SEND] info = %s,",mqEntity.toString());
                }
                return mqEntity;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //producer.shutdown();
            }

        }
        return mqEntity;
    }

    @Override
    public boolean subscribe(MqEntity.Key key, CallMe callMe) {
        return pullMessageAndDoJob(key.getTopic(), key.getTags(), callMe, "");
    }

    @Override
    public boolean subscribe(MqEntity.Key key, String eventKey) {
        return pullMessageAndDoJob(key.getTopic(), key.getTags(), null, eventKey);
    }

    /**
     * PULL服务端MQ的消息,并执行真正的业务逻辑
     *
     * @param topic    消息主题
     * @param callMe   回调方法
     * @param eventKey 回调的事件KEY
     * @return
     */
    private boolean pullMessageAndDoJob(String topic, String tags, CallMe callMe, String eventKey) {
        DefaultMQPushConsumer consumer = buildConsumer();

        if (consumer != null) {
            try {
                consumer.subscribe(topic, tags);
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                MessageListenerConcurrently listener = (msgList, context) -> {
                    if (null != msgList && msgList.size() > 0) {
                        try {
                            MessageExt msg = msgList.get(0);
                            logger.debug("[RECV] MessageExt Size:(%s), -> %s", msgList.size(),msg.toString());
                            MqEntity msgEntity = serializer.getObj(msg.getBody());
                            msgEntity.setReaded(true);
                            msgEntity.setSended(true);
                            //执行真正的业务
                            if (null!= callMe) {
                                callMe.exec(msgEntity);
                            } else {
                                eventBus.triggerAsync(eventKey, msgEntity);
                            }
                        } catch (Exception e) {
                            //NOTE: ROCKET MQ 一但报错就会造成,消息的重复,垃圾.
                            logger.error("[RECV] " + Thread.currentThread().getName() +  e.getMessage());
                            e.printStackTrace();
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
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


    private DefaultMQProducer buildProducer() {
        if(null == this.producer){
            String groupName = MqConfig.rocketmq_producergroupname.get() ;
            DefaultMQProducer producer = new DefaultMQProducer(groupName);
            producer.setNamesrvAddr(MqConfig.rocketmq_namesrvaddr.get());
            producer.setInstanceName(MqConfig.rocketmq_producer.get());
            producer.setVipChannelEnabled(false);
            try {
                producer.start();
                this.producer = producer;
            } catch (MQClientException e) {
                producer.shutdown();
                e.printStackTrace();
            }
        }

        return producer;
    }


    private DefaultMQPushConsumer buildConsumer() {
        String groupName = MqConfig.rocketmq_consumergroupname.get() + String.valueOf(System.currentTimeMillis()); //感觉概本就不是用户组呀:(
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);  //实质上还是拉取消息,无语
        consumer.setNamesrvAddr(MqConfig.rocketmq_namesrvaddr.get());
        consumer.setInstanceName(MqConfig.rocketmq_consumer.get());
        consumer.setVipChannelEnabled(false);
        return consumer;
    }


}
