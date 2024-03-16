package com.kai.kairpc.core.api;

import java.util.List;

/**
 * 负载均衡器：流量分发
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer DEFAULT = providers -> (providers == null || providers.size() == 0) ? null : providers.get(0);

}
