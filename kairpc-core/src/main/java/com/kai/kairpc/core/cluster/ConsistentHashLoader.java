package com.kai.kairpc.core.cluster;

import com.kai.kairpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoader<T> implements LoadBalancer<T> {

    private static final int VIRTUAL_NODE_SIZE = 100;

    private final TreeMap<Integer, T> virtualNodeMap = new TreeMap<>();

    @Override
    public T choose(List<T> providers) {
        if (providers.isEmpty()) {
            return null;
        }

        // 构建虚拟节点，将节点均匀化
        // 每次都需要重新计算，是为了应对节点下线或者上线的情形
        for (T provider : providers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                int hash = hash(provider + "#" + i);
                virtualNodeMap.put(hash, provider);
            }
        }

        // TODO: 获取请求 hash
        int hash = hash("request");

        // 选择 hash 最接近且大于等于请求 hash 的虚拟节点
        Map.Entry<Integer, T> entry = virtualNodeMap.ceilingEntry(hash);
        if (entry == null) {
            entry = virtualNodeMap.firstEntry();
        }
        return entry.getValue();
    }

    private int hash(Object key) {
        return key.hashCode();
    }
}