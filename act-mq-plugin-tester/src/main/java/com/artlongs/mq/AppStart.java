package com.artlongs.mq;

import act.Act;
import act.app.App;
import act.controller.Controller;
import act.event.EventBus;
import act.event.On;
import act.event.OnEvent;
import org.joda.time.DateTime;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.result.Result;

import javax.inject.Inject;

@Controller
public class AppStart extends Controller.Util{

    private static Logger logger = L.get(AppStart.class);

    @Inject
    private MQ mq;

    @Inject
    private EventBus eventBus;


    public static void main(String[] args) throws Exception {
        Act.start("mq","com.artlongs.mq");
    }


    @GetAction("/mq/redis")
    public Result mqRedis() {
        // 接收消息,并回调执行
        boolean isReceived = mq.subscribe(MqEntity.Key.ofRedis("mq:topic") ,"show_topic_1");

        MqEntity msgEntity = new MqEntity(MqEntity.Key.ofRedis("mq:topic"), "");
        logger.info("test mq send");
        for (int i = 0; i < 1; i++) {
            String msg = " message for mq-redis:"+i;
            msgEntity = mq.send(msgEntity.setMsg(msg),  MQ.Spread.TOPIC);
        }



        return renderJson(msgEntity);
    }

    @GetAction("/mq/rabbit")
    public Result mqrabbit() {
        // 接收消息,并回调执行
        boolean isReceived = mq.subscribe(MqEntity.Key.ofRabbitDefault("topic") ,"show_topic_1");

        MqEntity msgEntity = new MqEntity(MqEntity.Key.ofRabbitDefault("topic"), "");
        logger.info("test mq send");
        for (int i = 0; i < 1; i++) {
            String msg = " test"+i;
            msgEntity = mq.send(msgEntity.setMsg(msg),  MQ.Spread.TOPIC);
        }

        return renderJson(msgEntity);
    }


    @GetAction("/mq/rocket")
    public Result rocketmq() {
        logger.info("test mq send");
        // 接收消息,并回调执行
        boolean isReceived = mq.subscribe(MqEntity.Key.ofRocket("topic",""), "show_topic_1");

        MqEntity msgEntity = mq.send(new MqEntity(MqEntity.Key.ofRocket("topic",""),"hello"));

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
