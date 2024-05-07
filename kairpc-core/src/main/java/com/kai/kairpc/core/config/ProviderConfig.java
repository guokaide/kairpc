package com.kai.kairpc.core.config;

import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.provider.ProviderBootstrap;
import com.kai.kairpc.core.provider.ProviderInvoker;
import com.kai.kairpc.core.registry.zk.ZkRegistryCenter;
import com.kai.kairpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * 1. 将 Provider 需要的配置类导入到 Spring 容器中
 * 2. 将 注册中心注入到 Spring 容器中，用于注册 Provider
 * 3. 将 Provider 启动类(ProviderBootstrap)注入到 Spring 容器中，并在本地注册服务
 * 4. 将 Provider 请求处理类(ProviderInvoker)注入到 Spring 容器中，用于处理请求
 * SpringBootTransport (接收 HTTP 请求) -> ProviderInvoker（处理 HTTP 请求）
 * 5. 启动 Provider 启动类，启动注册中心，将本地服务注册到注册中心，对外提供服务
 */
@Slf4j
@Configuration
@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private String port;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter providerRegisterCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("providerBootstrapRunner start ...");
            providerBootstrap.start();
            log.info("providerBootstrapRunner started ...");
        };
    }

}
