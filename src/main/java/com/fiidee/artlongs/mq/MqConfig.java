package com.fiidee.artlongs.mq;

import act.app.conf.AppConfigurator;
import act.app.conf.AutoConfig;
import org.osgl.$;
import org.osgl.util.Const;


/**
 * 取得MQ的基础配置
 * Created by leeton on 8/19/17.
 */
@AutoConfig("mq")
public class MqConfig {

    //MQ 实例
    public static final String provider_redis = "redis";
    public static final String provider_activemq = "activemq";
    public static final String provider_rocketmq = "rocketmq";
    public static final String provider_zmq = "zmq";
    public static final String provider_rabbitmq = "rabbitmq";
    //序列化方案
    public static final String serializer_fst = "fst";
    public static final String serializer_kryo = "kryo";
    public static final String serializer_fastjson = "fastjson";
    public static final String serializer_gson = "gson";
    public static final String serializer_codejson = "codejson";
    //provider && serializer
    public static final Const<String> provider = $.constant("");
    public static final Const<String> serializer = $.constant("");
    //rabbitmq
    public static final Const<String> rabbitmq_username = $.constant("");
    public static final Const<String> rabbitmq_password = $.constant("");
    public static final Const<String> rabbitmq_serverip = $.constant("");
    public static final Const<Integer> rabbitmq_port = $.constant(0);
    public static final Const<String> rabbitmq_virtualhost = $.constant("");
    public static final Const<Boolean> rabbitmq_ssl = $.constant(false);
    //rocketmq
    public static final Const<String> rocketmq_username = $.constant("");
    public static final Const<String> rocketmq_password = $.constant("");
    public static final Const<String> rocketmq_namesrvaddr = $.constant("");
    public static final Const<String> rocketmq_producergroupname = $.constant("");
    public static final Const<String> rocketmq_producer = $.constant("");
    public static final Const<String> rocketmq_consumergroupname = $.constant("");
    public static final Const<String> rocketmq_consumer = $.constant("");
    //redis
    public static final Const<String> redis_username = $.constant("");
    public static final Const<String> redis_password = $.constant("");
    public static final Const<String> redis_host = $.constant("");
    public static final Const<Integer> redis_port = $.constant(0);
    public static final Const<Integer> mq_redis_connetion_timeout = $.constant(0);
    public static final Const<Integer> redis_database = $.constant(0);

}
