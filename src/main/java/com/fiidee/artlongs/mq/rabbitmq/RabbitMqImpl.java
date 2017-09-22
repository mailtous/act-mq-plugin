package com.fiidee.artlongs.mq.rabbitmq;

import act.event.EventBus;
import com.fiidee.artlongs.mq.MQ;
import com.fiidee.artlongs.mq.MqConfig;
import com.fiidee.artlongs.mq.MsgEntity;
import com.fiidee.artlongs.mq.serializer.ISerializer;
import com.fiidee.artlongs.mq.tools.BeanCopy;
import com.fiidee.artlongs.mq.tools.SnowflakeIdWorker;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.osgl.logging.L;
import org.osgl.logging.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Rabbitmq 实现
 * Created by leeton on 8/18/17.
 */
public class RabbitMqImpl implements MQ {
    private static Logger logger = L.get(RabbitMqImpl.class);

    public final static String default_exchange = "default_exchange";
    public final static String default_queue = "default_queue";

    private Connection connection;

    private static EventBus eventBus;

    private static SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);
    ; // Twitter_Snowflake ID生成器

    private static ISerializer serializer;


    public RabbitMqImpl init(EventBus eventBus, ISerializer serializer) {
        getConnection();
        this.eventBus = eventBus;
        this.serializer = serializer;
        return this;
    }

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String topic, SendType sendType) {
        MsgEntity msgEntity = new MsgEntity();
        msgEntity.setMsg(msg);
        msgEntity.setTopic(topic);
        msgEntity.setSendType(sendType);
        msgEntity.setQueue(default_queue);
        msgEntity.setExchange(default_exchange);
        return send(msgEntity);
    }

    @Override
    public <MODEL> MsgEntity send(MODEL msg, String exchangeName, String queueName, String topic, SendType sendType) {
        MsgEntity msgEntity = new MsgEntity();
        msgEntity.setMsg(msg);
        msgEntity.setExchange(exchangeName);
        msgEntity.setQueue(queueName);
        msgEntity.setTopic(topic);
        msgEntity.setSendType(sendType);
        return send(msgEntity);
    }


    private <MODEL> MsgEntity send(MsgEntity msgEntity) {
        Channel channel = getChannel(msgEntity.getExchange(), msgEntity.getSendType());

        if (channel == null) {
            msgEntity.setSended(false);
            return msgEntity;
        }

        try {
            //String queueName = channel.queueDeclare().getQueue(); //匿名队列
            boolean durable = true; //Server端的Queue持久化
            String queue = StringUtils.defaultString(msgEntity.getQueue(), default_queue);
            String queueName = channel.queueDeclare(queue, durable, false, false, null).getQueue();
            queueBind(channel, msgEntity.getExchange(), queueName, msgEntity.getTopic());
            //限制发给同一个消费者不得超过1条消息
            int prefetchCount = 1;
            channel.basicQos(prefetchCount);
            //设置消息ID,有需要的话可以通过id来进行消息排序
            String messageId = new String(System.nanoTime() + "");
            AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder().messageId(messageId).build();
            logger.debug(" [SEND] =" + msgEntity);
            channel.basicPublish(msgEntity.getExchange(), msgEntity.getTopic(), basicProperties, serializer.getByte(msgEntity));

            msgEntity.setSended(true);
            closeChannel(channel);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgEntity;
    }

    @Override
    public boolean subscribe(String exchangeName, String queueName, String topic, CallMe callMe) {
        try {
            Channel channel = getChannelAndSetting(exchangeName, queueName, topic);
            //QueueingConsumer consumer = new QueueingConsumer(channel);
            Receiver consumer = buildConsumerAndGetMessage(exchangeName, queueName, channel, callMe);

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, consumer);

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean subscribe(String exchangeName, String queueName, String topic, String eventKey) {
        try {
            Channel channel = getChannelAndSetting(exchangeName, queueName, topic);
            Receiver consumer = buildConsumerAndGetMessage(exchangeName, queueName, channel, eventKey);

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, consumer);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Channel getChannelAndSetting(String exchangeName, String queueName, String topic) {
        Channel channel = getChannel(exchangeName, SendType.TOPIC);
        if (null != channel) {
            try {
                // 订阅某个关键词，绑定到匿名Queue中
                queueBind(channel, exchangeName, queueName, topic);

                //限制发给同一个消费者不得超过1条消息
                int prefetchCount = 1;
                channel.basicQos(prefetchCount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return channel;
    }

    private void queueBind(Channel channel, String exchangeName, String queueName, String topic) {
        String queue = StringUtils.defaultString(queueName, default_queue);
        String exchange = StringUtils.defaultString(exchangeName, default_exchange);
        try {
            channel.queueBind(queue, exchange, topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建消费者并监听消息,收到消息后回调要执行的方法
     *
     * @param exchangeName 转换器
     * @param queueName    消息队列
     * @param channel      消息通道
     * @param callMe       回调(做你想做的事)
     * @return
     */
    private Receiver buildConsumerAndGetMessage(String exchangeName, String queueName, Channel channel, CallMe callMe) {
        Receiver consumer = (String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) -> {
            if (null != body) {
                MsgEntity msgEntity = getMessage(body);
                boolean autoAck = false;
                logger.debug("[RECE] '" + envelope.getRoutingKey() + "':'" + msgEntity.getMsg() + " id: " + properties.getMessageId());
                channel.basicAck(envelope.getDeliveryTag(), false);//手动确认已收到消息
                msgEntity.setReaded(true);
                callMe.exec(msgEntity);
            }
        };

        return consumer;
    }


    /**
     * (推荐)创建消费者并监听消息,收到消息后再发布内部事件
     *
     * @param exchangeName 转换器
     * @param queueName    消息队列
     * @param channel      消息通道
     * @param eventKey     内部事件KEY,使用@On("enventKey"),去监听事件
     * @return
     */
    private Receiver buildConsumerAndGetMessage(String exchangeName, String queueName, Channel channel, String eventKey) {
        Receiver consumer = (String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) -> {
            if (null != body) {
                MsgEntity msgEntity = getMessage(body);
                boolean autoAck = false;
                logger.debug("[RECE] '" + envelope.getRoutingKey() + "':'" + msgEntity.getMsg());
                channel.basicAck(envelope.getDeliveryTag(), false);//手动确认已收到消息
                msgEntity.setReaded(true);
                eventBus.emitAsync(eventKey, msgEntity, DateTime.now());
            }
        };
        return consumer;
    }

    public MsgEntity getMessage(byte[] body) {
        MsgEntity msgEntity = new MsgEntity();
        Object obj = new Object();
        ;
        msgEntity.setMsg("");
        if (body == null) return msgEntity;
        try {
            obj = serializer.getObj(body);
            return (MsgEntity) obj;
        } catch (Exception e) { //转换类型失败,则使用BEAN COPY
            BeanCopy.copy(obj, msgEntity);
        }
        return msgEntity;
    }


    /**
     * 取得消息系统连接
     *
     * @return
     */
    private Connection getConnection() {
        if (connection == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(MqConfig.rabbitmq_serverip.get());
            factory.setPort(MqConfig.rabbitmq_port.get());

            if (StringUtils.isNotBlank(MqConfig.rabbitmq_virtualhost.get())) {
                factory.setVirtualHost(MqConfig.rabbitmq_virtualhost.get());
            }
            if (StringUtils.isNotBlank(MqConfig.rabbitmq_username.get())) {
                factory.setUsername(MqConfig.rabbitmq_username.get());
            }

            if (StringUtils.isNotBlank(MqConfig.rabbitmq_password.get())) {
                factory.setPassword(MqConfig.rabbitmq_password.get());
            }

            try {
                connection = factory.newConnection();
            } catch (Exception e) {
                throw new RuntimeException("can not connection rabbitmq server", e);
            }
        }

        return connection;
    }

    /**
     * 创建通道及设置消息传播方式
     *
     * @param exchangeName
     * @param sendType
     * @return
     */
    private Channel getChannel(String exchangeName, SendType sendType) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            if (null != channel) {
                channel.exchangeDeclare(exchangeName, getRabbitMqExchangeType(sendType));
            }

        } catch (IOException e) {
            throw new RuntimeException("[x] can not create channel", e);
        }

        return channel;
    }

    private BuiltinExchangeType getRabbitMqExchangeType(SendType sendType) {
        if (sendType == SendType.FANOUT) {
            return BuiltinExchangeType.FANOUT;
        }
        if (sendType == SendType.TOPIC) {
            return BuiltinExchangeType.TOPIC;
        }
        return BuiltinExchangeType.TOPIC;
    }


    public static void sleep(long timeMillis) {
        try {
            Thread.sleep(timeMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void closeChannel(Channel channel) {
        try {
            // 关闭频道
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 阅后即焚
     *
     * @param lists
     * @return
     */
    public <T> T readAndFire(List<T> lists, int index) {
        Object o = new Object();
        if (!lists.isEmpty()) {
            o = lists.get(index);
            remove(lists, o);
        }
        return (T) o;
    }

    public void remove(List list, Object o) {
        if (!list.isEmpty()) {
            Iterator it = list.iterator();
            while (it.hasNext()) { //删除已取走的对象
                if (it.next().equals(o)) it.remove();
            }
        }
    }

    public static CallMe toShow() {
        CallMe todo = (c) -> {
            System.err.println("[CALLME]: " + c);
        };
        return todo;
    }


}
