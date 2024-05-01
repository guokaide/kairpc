package com.kai.kairpc.core.filter;

import com.kai.kairpc.core.api.Filter;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;

/**
 * 上下文参数过滤器：用于处理跨服务调用参数
 */
public class ParamsFilter implements Filter {

    // 将上下文参数放入 RpcRequest 中
    @Override
    public Object preFilter(RpcRequest request) {
        request.getParams().putAll(RpcContext.getContextParameters());
        return null;
    }

    // 清理上下文，防止内存泄露以及污染上下文
    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.clearContextParameters();
        return null;
    }
}
