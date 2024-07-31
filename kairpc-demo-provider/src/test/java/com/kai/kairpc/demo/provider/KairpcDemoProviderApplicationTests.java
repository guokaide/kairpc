package com.kai.kairpc.demo.provider;

import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.apollo.mockserver.ApolloTestingServer;
import com.ctrip.framework.apollo.mockserver.MockApolloExtension;
import com.kai.kairpc.core.config.ProviderProperties;
import com.kai.kairpc.core.test.TestZkServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockApolloExtension.class)
class KairpcDemoProviderApplicationTests {

    static TestZkServer zkServer = new TestZkServer();
    static ApolloTestingServer apolloServer = new ApolloTestingServer();

    @Autowired
    ProviderProperties providerProperties;

    @SneakyThrows
    @BeforeAll
    static void init() {
        System.out.println("=== zk2182 ===");
        zkServer.start();
        System.out.println("=== apollo8080 ===");
        apolloServer.start();
    }

    @Test
    void contextLoads() {
        System.out.println("provider test start ...");
        System.out.println("ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE: " +
                System.getProperty(ApolloClientSystemConsts.APOLLO_CONFIG_SERVICE));
        System.out.println("ApolloClientSystemConsts.APOLLO_META: " +
                System.getProperty(ApolloClientSystemConsts.APOLLO_META));
    }

    @Test
    void printProviderProperties() {
        System.out.println("providerProperties: " + providerProperties);
    }


    @AfterAll
    static void destroy() {
        zkServer.stop();
        apolloServer.close();
    }

}
