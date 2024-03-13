package com.kai.kairpc.core.consumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

    @Bean
    ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    // ApplicationRunner 会在所有的 Bean 初始化之后进行调用
    // 多个 ApplicationRunner 执行优先级不同，默认应用入口的优先级高于此处优先级，所以这里需要把优先级调到最高
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            System.out.println("consumerBootstrapRunner start ...");
            consumerBootstrap.start();
            System.out.println("consumerBootstrapRunner started ...");
        };
    }
}
