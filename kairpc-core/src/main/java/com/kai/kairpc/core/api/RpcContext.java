package com.kai.kairpc.core.api;

import com.kai.kairpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RpcContext {

    List<Filter> filters;
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
    // 染色: kairpc.color = gray
    // 追踪：kairpc.g_trace_id
    // gw -> service1 -> service2（跨线程传递）
    private Map<String, String> parameters = new ConcurrentHashMap<>();

}
