package com.artlongs.mq;


import com.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.artlongs.mq.tools.ID;

import java.io.Serializable;

/**
 * 消息实体
 * Created by leeton on 8/18/17.
 */
public class MqEntity<M> implements Serializable {
    private static final long serialVersionUID = 1L;
    private MqEntity(){}
//======================
    private String id;
    private Key key;         //消息 KEY
    private M msg;          //消息内容
    private MQ.Spread spread = MQ.Spread.TOPIC;  //消息传播方式(通用)
    private String serverip;   //消息服务器IP(通用)
    private boolean isSended = false; //已发送
    private boolean isReaded = false; //已接收

    public static MqEntity ofDef(Object msg){
        MqEntity entity = new MqEntity();
        entity.id = ID.ONLY.id();
        entity.msg = msg;
        return entity;
    }

    public MqEntity(Key key, M msg) {
        this.id = ID.ONLY.id();
        this.key = key;
        this.msg = msg;
        this.spread = MQ.Spread.TOPIC;
    }

    public MqEntity(String id, Key key, M msg) {
        this.id = id;
        this.key = key;
        this.msg = msg;
        this.spread = MQ.Spread.TOPIC;
    }

    public MqEntity(String id, Key key, M msg, MQ.Spread spread) {
        this.id = id;
        this.key = key;
        this.msg = msg;
        this.spread = spread;
    }

    /**
     * 消息的 KEY
     */
    public static class Key implements Serializable{
        private static final long serialVersionUID = 1L;
        private Key(){}

        private String exchange;   // 交换器,RABBITMQ才会使用到
        private String queue;      // 队列 ,RABBITMQ才会使用到
        private String topic;      // 消息主题(通用)
        private String tags;       // 消息标签

        public static Key ofRabbit(String exchange, String queue, String topic) {
            Key key = new Key();
            key.setExchange(exchange);
            key.setQueue(queue);
            key.setTopic(topic);
            return key;
        }

        public static Key ofRabbitDefault(String topic) {
            Key key = new Key();
            key.setExchange(RabbitMqImpl.default_exchange);
            key.setQueue(RabbitMqImpl.default_queue);
            key.setTopic(topic);
            return key;
        }

        public static Key ofRocket(String topic, String tags) {
            Key key = new Key();
            key.setTopic(topic);
            key.setTags(tags);
            return key;
        }

        public static Key ofRedis(String topic) {
            Key entity = new Key();
            entity.setTopic(topic);
            return entity;
        }
       //==============================================
        public String getExchange() {
            return exchange;
        }

        public Key setExchange(String exchange) {
            this.exchange = exchange;
            return this;
        }

        public String getQueue() {
            return queue;
        }

        public Key setQueue(String queue) {
            this.queue = queue;
            return this;
        }

        public String getTopic() {
            return topic;
        }

        public Key setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public String getTags() {
            return tags;
        }

        public Key setTags(String tags) {
            this.tags = tags;
            return this;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Key{");
            sb.append("exchange='").append(exchange).append('\'');
            sb.append(", queue='").append(queue).append('\'');
            sb.append(", topic='").append(topic).append('\'');
            sb.append(", tags='").append(tags).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    // ============ getter && setter ======================


    public String getId() {
        return id;
    }

    public M getMsg() {
        return msg;
    }

    public MQ.Spread getSpread() {
        return spread;
    }

    public Key getKey() {
        return key;
    }

    public String getServerip() {
        return serverip;
    }

    public boolean isSended() {
        return isSended;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public MqEntity<M> setId(String id) {
        this.id = id;
        return this;
    }

    public MqEntity<M> setKey(Key key) {
        this.key = key;
        return this;
    }

    public MqEntity<M> setMsg(M msg) {
        this.msg = msg;
        return this;
    }

    public MqEntity<M> setSpread(MQ.Spread spread) {
        this.spread = spread;
        return this;
    }

    public MqEntity<M> setServerip(String serverip) {
        this.serverip = serverip;
        return this;
    }

    public MqEntity<M> setSended(boolean sended) {
        isSended = sended;
        return this;
    }

    public MqEntity<M> setReaded(boolean readed) {
        isReaded = readed;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MqEntity{");
        sb.append("id='").append(id).append('\'');
        sb.append(", key=").append(key.toString());
        sb.append(", msg=").append(msg.toString());
        sb.append(", spread=").append(spread);
        sb.append(", serverip='").append(serverip).append('\'');
        sb.append(", isSended=").append(isSended);
        sb.append(", isReaded=").append(isReaded);
        sb.append('}');
        return sb.toString();
    }
}



