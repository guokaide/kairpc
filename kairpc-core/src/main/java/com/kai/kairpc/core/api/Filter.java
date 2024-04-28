package com.kai.kairpc.core.api;

/**
 * 过滤器：前置处理、后置处理等
 */
public interface Filter {

    Object preFilter(RpcRequest request);

    Object postFilter(RpcRequest request, RpcResponse response, Object result);

    // FilterChain 实现方式
    // 1. next()
    // 2. array
//    Filter next();

    Filter DEFAULT = new Filter() {
        @Override
        public Object preFilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };
}
