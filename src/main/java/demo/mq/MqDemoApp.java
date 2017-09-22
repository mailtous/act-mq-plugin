package demo.mq;

import act.Act;
import act.controller.Controller;
import act.event.EventBus;
import act.event.On;
import act.job.OnAppStart;
import com.fiidee.artlongs.mq.MQ;
import com.fiidee.artlongs.mq.MqReceiver;
import com.fiidee.artlongs.mq.MsgEntity;
import com.fiidee.artlongs.mq.rabbitmq.RabbitMqImpl;
import com.fiidee.artlongs.mq.rocketmq.RocketMqImpl;
import org.joda.time.DateTime;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.result.Result;

import javax.inject.Inject;

public class MqDemoApp extends Controller.Util{

    private static Logger logger = L.get(MqDemoApp.class);

    @Inject
    private MQ mq;

    @Inject
    private EventBus eventBus;

    @GetAction("/mq/send")
    public Result mq() {
        // 接收消息,并回调执行
        boolean isReceived = mq.subscribe(RabbitMqImpl.default_exchange,RabbitMqImpl.default_queue,"topic","show_topic_1");

        MsgEntity msgEntity = new MsgEntity();
        logger.info("test mq send");
        for (int i = 0; i < 1; i++) {
            String msg = " test"+i;
            msgEntity = mq.send(msg, "topic", MQ.SendType.TOPIC);
        }

        //  eventBus.trigger("test_event", "test_msg");
        //接收消息,并发布事件来执行
       //boolean isReceived = mq.subscribe(RabbitMqImpl.default_exchange, RabbitMqImpl.default_queue, "topic", "show_topic_1");

        return renderJson(msgEntity);
    }


    @GetAction("rocketmq")
    public Result rocketmq() {
        logger.info("test mq send");
        // 接收消息,并回调执行
        boolean isReceived = mq.subscribe(RabbitMqImpl.default_exchange,RabbitMqImpl.default_queue,"topic", RocketMqImpl.toShow());

        //  eventBus.trigger("test_event", "test_msg");
        //接收消息,并发布事件来执行
        //boolean isReceived = mq.subscribe(RabbitMqImpl.default_exchange, RabbitMqImpl.default_queue, "topic", "show_topic_1");

        MsgEntity msgEntity = mq.send("test", "topic", MQ.SendType.TOPIC);

        return renderJson("");
    }


    /**
     *  测试消息接收
     * @param msgEntity
     * @param dateTime
     */
    @On("show_topic_1")
    public static void onMessageShow(Object msgEntity, DateTime dateTime){
        System.err.println(" SHOW OF EVENT :" + msgEntity);
    }

    @GetAction("/u")
    public Result signUp() {
        logger.info(">> user sign up");
        eventBus.trigger("show_topic_1", "hello,leeton.", DateTime.now());
       return renderJson("OK");
    }

    @OnAppStart
    public void doStarted(){
        System.err.println("do Started " );
/*        App app = app();
        app.scannerManager().register(new HelloListenerByteCodeScanner());
        AppByteCodeScanner scanner = app.scannerManager().byteCodeScannerByClass(HelloListenerByteCodeScanner.class);
        scanner.start("MqDemoApp");*/
    }

    /**
     * 如何把Hello的name 转给加注解的方法
     * @param name
     * @return
     */
    @GetAction("/hello")
    @Hello("leeton")
    public Result doHello(String name){
        System.err.println("say hello :" + name );
        return renderJson("hello"+name);
    }


    @MqReceiver("topic")
    public Result getMq(MsgEntity msg) {
        System.err.println("say hello :" + msg);
        return renderJson("hello" + msg);
    }

    public static void main(String[] args) throws Exception {
        Act.start("mq");
    }


}
