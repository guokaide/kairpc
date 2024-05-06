package com.kai.kairpc.core.annotation;

import com.kai.kairpc.core.consumer.ConsumerConfig;
import com.kai.kairpc.core.provider.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 RPC 功能
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableRpc {
}
