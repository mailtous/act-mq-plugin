package demo.mq;

/**
 * Created by leeton on 9/18/17.
 */
public class HelloModule extends org.osgl.inject.Module  {
    @Override
    protected void configure() {
        bind(IHello.class).to(HelloService.class).withAnnotation(Hello.class);
    }
}
