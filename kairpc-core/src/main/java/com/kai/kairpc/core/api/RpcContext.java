package com.kai.kairpc.core.api;

import com.kai.kairpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {

    List<Filter> filters;
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
}
