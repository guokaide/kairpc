package com.kai.kairpc.core.api;

import com.kai.kairpc.core.meta.InstanceMeta;

import java.util.List;

/**
 * 负载均衡器：流量分发
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer<InstanceMeta> DEFAULT = providers ->
            (providers == null || providers.size() == 0) ? null : providers.get(0);

}
