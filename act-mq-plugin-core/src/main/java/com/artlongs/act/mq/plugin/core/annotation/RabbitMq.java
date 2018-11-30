package com.artlongs.act.mq.plugin.core.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定 RabbitMq 服务
 * Created by leeton on 2018/11/30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
@Qualifier
public @interface RabbitMq {
}
