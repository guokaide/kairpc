package com.kai.kairpc.core.annotation;

import java.lang.annotation.*;

/**
 * 服务提供者
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface KaiProvider {
}
