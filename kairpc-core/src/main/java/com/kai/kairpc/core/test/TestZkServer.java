package com.kai.kairpc.core.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

/**
 * Test ZooKeeper Server
 */
@Slf4j
public class TestZkServer {

    TestingCluster cluster;

    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2182,
                -1, -1, true, -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        log.info("TestZooKeeperServer starting...");
        cluster.start();
        cluster.getServers().forEach(s -> log.info(s.getInstanceSpec().toString()));
        log.info("TestZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        log.info("TestZooKeeperServer stopping...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        log.info("TestZooKeeperServer stopped.");
    }
}
