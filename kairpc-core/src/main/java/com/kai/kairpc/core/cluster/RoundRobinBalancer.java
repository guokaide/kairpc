package com.kai.kairpc.core.cluster;

import com.kai.kairpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return providers.get((index.getAndIncrement() & 0x7fffffff) % providers.size());
    }
}
