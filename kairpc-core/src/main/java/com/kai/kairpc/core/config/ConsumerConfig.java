package com.kai.kairpc.core.config;

import com.kai.kairpc.core.api.*;
import com.kai.kairpc.core.cluster.GrayRouter;
import com.kai.kairpc.core.cluster.RoundRobinBalancer;
import com.kai.kairpc.core.consumer.ConsumerBootstrap;
import com.kai.kairpc.core.filter.ParamsFilter;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 1. 将 Consumer 需要的配置类导入到 Spring 容器中
 * 2. 将 注册中心注入到 Spring 容器中，用于获取 Provider 实例
 * 3. 将 Consumer 启动类(ConsumerBootstrap)注入到 Spring 容器中
 * 4. 将 LoadBalancer、Router、Filter、RpcContext 注入到 Spring 容器中
 * 5. 启动 Consumer 启动类
 * - 在注册中心获取服务实例
 * - 扫描 @KaiConsumer 注解，创建该注解标记的属性的服务代理类，用于向 Provider 发起远程调用
 * Note: @KaiConsumer 注解可以替换为 @Autowired，远程调用就变成了本地调用
 */
@Slf4j
@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    AppProperties appProperties;

    @Autowired
    ConsumerProperties consumerProperties;

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
            log.info("consumerBootstrapRunner start ...");
            consumerBootstrap.start();
            log.info("consumerBootstrapRunner started ...");
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRegisterCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    public ApolloChangedListener consumerApolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRobinBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

    @Bean
    public Filter defaultFilter() {
        return new ParamsFilter();
    }


    // TODO: 在这里 RpcContext 就一个，所有的请求都是使用这一个 RpcContext, 怎么才能每个请求都可以使用自己的 RpcContext 呢？
    // TODO: 1. 每次使用的时候都直接 new 1 个新的出来
    // TODO: 2. RpcContext 提供一个 clone() 方法，每次使用的时候 clone 一个新的出来，需要设置请求特定的参数的时候，使用的时候在新的 RpcContext 中设置即可。    @Bean
    @Bean
    public RpcContext rpcContext(@Autowired Router router,
                                 @Autowired LoadBalancer loadBalancer,
                                 @Autowired List<Filter> filters) {
        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);
        rpcContext.setFilters(filters);
        rpcContext.setAppProperties(appProperties);
        rpcContext.setConsumerProperties(consumerProperties);
        return rpcContext;
    }

}
