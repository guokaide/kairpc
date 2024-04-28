package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.core.test.TestZkServer;
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
    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
        applicationContext = SpringApplication.run(KairpcDemoProviderApplication.class,
                "--server.port=8084",
                "--kairpc.zkServer=localhost:2182",
                "--logging.level.com.kai.kairpc=debug");
    }

    @Test
    void contextLoads() {
        System.out.println("consumer test start...");
    }

    @AfterAll
    static void destroy() {
        SpringApplication.exit(applicationContext, () -> 1);
        zkServer.stop();
    }
}
