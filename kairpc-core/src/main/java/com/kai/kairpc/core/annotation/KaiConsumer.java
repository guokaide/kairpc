package com.kai.kairpc.core.annotation;

import java.lang.annotation.*;

/**
 * 服务消费者
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface KaiConsumer {
}
