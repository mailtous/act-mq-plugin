package demo.mq;

import act.inject.AutoBind;

/**
 * Created by leeton on 9/18/17.
 */
public class HelloService implements IHello {
    public void HelloService(Hello hello) {
        System.out.printf("SAY : "+hello.value());
    }
}
