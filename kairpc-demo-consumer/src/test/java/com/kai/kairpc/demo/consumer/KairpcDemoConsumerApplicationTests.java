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
        System.out.println(" ================================== ");
        System.out.println(" ============ zk 2182 ============= ");
        System.out.println(" ================================== ");
        zkServer.start();
        System.out.println(" ================================== ");
        System.out.println(" ============  p 2094 ============= ");
        System.out.println(" ================================== ");
        applicationContext = SpringApplication.run(KairpcDemoProviderApplication.class,
                "--server.port=8084",
                "--kairpc.zk.server=localhost:2182",
                "--kairpc.zk.root=kairpc",
                "--kairpc.app.env=test",
                "--logging.level.com.kai.kairpc=info",
                "--kairpc.provider.metas.dc=bj",
                "--kairpc.provider.metas.gray=false",
                "--kairpc.provider.metas.unit=B001");
        System.out.println(" ================================== ");
        System.out.println(" ============  p 2095 ============= ");
        System.out.println(" ================================== ");
        applicationContext = SpringApplication.run(KairpcDemoProviderApplication.class,
                "--server.port=8085",
                "--kairpc.zk.server=localhost:2182",
                "--kairpc.zk.root=kairpc",
                "--kairpc.app.env=test",
                "--logging.level.com.kai.kairpc=info",
                "--kairpc.provider.metas.dc=bj",
                "--kairpc.provider.metas.gray=false",
                "--kairpc.provider.metas.unit=B002");
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
