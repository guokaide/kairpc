package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.test.TestZkServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KairpcDemoProviderApplicationTests {

    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println("provider test start ...");
    }


    @AfterAll
    static void destroy() {
        zkServer.stop();
    }

}
