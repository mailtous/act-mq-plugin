package demo.mq;

import act.app.event.AppEventId;
import act.util.AnnotatedClassFinder;

/**
 * Created by leeton on 9/20/17.
 */
public class HelloFinder {
    @AnnotatedClassFinder(value = Hello.class)
    public static void foundStateless(Class<?> cls) {
        System.out.printf("Finde hello.class");
    }
}
