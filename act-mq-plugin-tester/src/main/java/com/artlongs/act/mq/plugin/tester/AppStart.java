package com.artlongs.act.mq.plugin.tester;

import act.Act;
import act.app.App;
import act.controller.Controller;
import act.event.EventBus;
import act.event.On;
import com.artlongs.act.mq.plugin.core.MQ;
import com.artlongs.act.mq.plugin.core.MqEntity;
import com.artlongs.act.mq.plugin.core.MqReceiver;
import com.artlongs.act.mq.plugin.core.annotation.RabbitMq;
import com.artlongs.act.mq.plugin.core.annotation.RedisMq;
import com.artlongs.act.mq.plugin.core.annotation.RocketMq;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.result.Result;

import javax.inject.Inject;
import javax.inject.Named;

@Controller
public class AppStart extends Controller.Util{

    private static Logger logger = L.get(AppStart.class);

    @RedisMq
    private MQ redismq;

    @RabbitMq
    private MQ rabbitmq;

    @RocketMq
    private MQ rocketmq;

    @Inject
    private EventBus eventBus;


    public static void main(String[] args) throws Exception {
        Act.start("act-mq-plugin-tester","com.artlongs");
    }


    @GetAction("/mq/redis")
    public Result mqRedis() {
        // 接收消息,并回调执行
        boolean isReceived = redismq.subscribe(MqEntity.Key.ofRedis("mq:topic") ,"show_topic_1");

        MqEntity msgEntity = new MqEntity(MqEntity.Key.ofRedis("mq:topic"), "");
        logger.info("test mq send");
        for (int i = 0; i < 1; i++) {
            String msg = " message for mq-redis:"+i;
            msgEntity = redismq.send(msgEntity.setMsg(msg),  MQ.Spread.FANOUT);
        }

        return renderJson(msgEntity);
    }

    @GetAction("/mq/rabbit")
    public Result mqrabbit() {
        // 接收消息,并回调执行
        boolean isReceived = rabbitmq.subscribe(MqEntity.Key.ofRabbitDefault("topic") ,"show_topic_1");

        MqEntity msgEntity = new MqEntity(MqEntity.Key.ofRabbitDefault("topic"), "");
        logger.info("test mq send");
        for (int i = 0; i < 1; i++) {
            String msg = " test"+i;
            msgEntity = rabbitmq.send(msgEntity.setMsg(msg),  MQ.Spread.TOPIC);
        }

        return renderJson(msgEntity);
    }


    @GetAction("/mq/rocket")
    public Result rocketmq() {
        logger.info("test mq send");
        // 接收消息,并回调执行
        boolean isReceived = rocketmq.subscribe(MqEntity.Key.ofRocket("topic",""), "show_topic_1");

        MqEntity msgEntity = rocketmq.send(new MqEntity(MqEntity.Key.ofRocket("topic",""),"hello"));

        return renderJson(msgEntity);
    }


    /**
     *  测试消息接收
     * @param msgEntity
     */
    @On(value = "show_topic_1")
    public void onMessageShow(MqEntity msgEntity){
        System.err.println(" SHOW OF REC EVENT :" + msgEntity);
    }

    @GetAction("/u")
    public Result signUp() {
        logger.info(">> user sign up");
        App.instance().eventBus().trigger("show_topic_1", MqEntity.ofDef("hello,leeton."));
       return renderJson("OK");
    }



    @MqReceiver("topic")
    public Result getMq(MqEntity msg) {
        System.err.println("say hello :" + msg);
        return renderJson("hello" + msg);
    }





}
