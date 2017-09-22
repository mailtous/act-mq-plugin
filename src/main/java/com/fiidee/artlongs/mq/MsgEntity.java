package com.fiidee.artlongs.mq;


import java.io.Serializable;

/**
 * 消息实体
 * Created by leeton on 8/18/17.
 */
public class MsgEntity<MODEL> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private MODEL msg;           //消息内容
    private boolean isSended = false; //已发送
    private boolean isReaded = false; //已接收
    private String exchange;   // 交换器,RABBITMQ才会使用到
    private String queue;      // 队列

    private String topic;      //消息主题(通用)
    private String serverip;   //消息服务器IP(通用)
    private MQ.SendType sendType; //消息传播方式(通用)


    public MsgEntity<MODEL> MsgEntity(MODEL msg) {
        setMsg(msg);
        return this;
    }

    public MsgEntity setMsg(MODEL msg) {
        this.msg = msg;
        return this;
    }

    // ============ getter && setter ======================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MODEL getMsg() {
        return msg;
    }

    public boolean isSended() {
        return isSended;
    }

    public void setSended(boolean sended) {
        isSended = sended;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public void setReaded(boolean readed) {
        isReaded = readed;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getServerip() {
        return serverip;
    }

    public void setServerip(String serverip) {
        this.serverip = serverip;
    }

    public MQ.SendType getSendType() {
        return sendType;
    }

    public void setSendType(MQ.SendType sendType) {
        this.sendType = sendType;
    }

    @Override
    public String toString() {
        return "msgEntity{" +
                "id='" + id + '\'' +
                ", msg=" + msg +
                ", isSended=" + isSended +
                ", isReaded=" + isReaded +
                ", exchange='" + exchange + '\'' +
                ", queue='" + queue + '\'' +
                ", topic='" + topic + '\'' +
                ", serverip='" + serverip + '\'' +
                ", sendType=" + sendType +
                '}';
    }
}



