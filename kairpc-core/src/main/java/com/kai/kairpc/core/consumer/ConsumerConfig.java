package com.kai.kairpc.core.consumer;

import com.kai.kairpc.core.api.LoadBalancer;
import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.api.Router;
import com.kai.kairpc.core.cluster.RoundRobinBalancer;
import com.kai.kairpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ConsumerConfig {

    @Value("${kairpc.providers}")
    String servers;

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

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRobinBalancer();
    }

    @Bean
    public Router router() {
        return Router.DEFAULT;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumerRegisterCenter() {
        return new ZkRegistryCenter();
    }
}
