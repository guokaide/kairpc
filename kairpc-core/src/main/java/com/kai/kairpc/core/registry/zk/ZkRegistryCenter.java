package com.kai.kairpc.core.registry.zk;

import com.kai.kairpc.core.api.KaiRpcException;
import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.meta.ServiceMeta;
import com.kai.kairpc.core.registry.ChangedListener;
import com.kai.kairpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * zk 注册中心
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;
    private final Map<ServiceMeta, TreeCache> treeCaches = new HashMap<>();

    @Value("${kairpc.zkServer}")
    private String server;

    @Value("${kairpc.zkRoot}")
    private String root;


    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(server)
                .namespace(root) // dubbo 的 group 是基于 zk namespace 实现的
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client connect to " + server + "/" + root + " starting...");
        client.start();
    }

    @Override
    public void stop() {
        treeCaches.values().forEach(TreeCache::close);
        treeCaches.clear();
        log.info(" ===> zk client stop...");
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, "service".getBytes());
            }
            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> register to zk: " + instancePath);
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, "provider".getBytes());
        } catch (Exception e) {
            throw new KaiRpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            log.info(" ===> unregister from zk: " + instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new KaiRpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("===> fetchAll from zk: " + servicePath + ", nodes: " + nodes);
            return mapInstances(nodes);
        } catch (Exception e) {
            throw new KaiRpcException(e);
        }
    }

    @NotNull
    private static List<InstanceMeta> mapInstances(List<String> nodes) {
        return nodes.stream().map(x -> {
            String[] strings = x.split("_");
            return InstanceMeta.http(strings[0], Integer.valueOf(strings[1]));
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        if (treeCaches.containsKey(service)) {
            return;
        }
        TreeCache treeCache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();
        treeCache.getListenable().addListener(
                (curatorFramework, treeCacheEvent) -> {
                    // service 有任何节点变动，这里都会执行
                    log.info(" ===> zk subscribe event: " + treeCacheEvent);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.handle(new Event(nodes));
                }
        );

        treeCache.start();

        treeCaches.put(service, treeCache);
    }
}
