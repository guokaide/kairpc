package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.demo.provider.KairpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class KairpcDemoConsumerApplicationTests {

    static ApplicationContext applicationContext;

    @BeforeAll
    static void init() {
        applicationContext = SpringApplication.run(KairpcDemoProviderApplication.class,
                "--server.port=8084", "--logging.level.com.kai.kairpc=debug");
    }

    @Test
    void contextLoads() {
        System.out.println("consumer test start...");
    }

    @AfterAll
    static void destroy() {
        SpringApplication.exit(applicationContext);
    }
}
