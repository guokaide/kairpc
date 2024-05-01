package com.kai.kairpc.core.api;

import com.kai.kairpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RpcContext {

    List<Filter> filters;
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
    private Map<String, String> parameters = new ConcurrentHashMap<>();

    // 上下文参数
    public static final ThreadLocal<Map<String, String>> CONTEXT_PARAMETERS = ThreadLocal.withInitial(HashMap::new);

    public static void setContextParameter(String key, String value) {
        CONTEXT_PARAMETERS.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return CONTEXT_PARAMETERS.get().get(key);
    }

    public static Map<String, String> getContextParameters() {
        return CONTEXT_PARAMETERS.get();
    }

    public static void clearContextParameters() {
        CONTEXT_PARAMETERS.get().clear();
    }

}
