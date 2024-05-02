package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.registry.zk.ZkRegistryCenter;
import com.kai.kairpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@Import({SpringBootTransport.class})
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
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
