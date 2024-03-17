package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.registry.ZkRegistryCenter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter providerRegisterCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(ProviderBootstrap providerBootstrap) {
        return x -> {
            System.out.println("providerBootstrapRunner start ...");
            providerBootstrap.start();
            System.out.println("providerBootstrapRunner started ...");
        };
    }
}
