package com.kai.kairpc.core.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kai.kairpc.core.api.*;
import com.kai.kairpc.core.consumer.http.OkHttpInvoker;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务消费者动态代理处理类
 */
@Slf4j
public class KaiInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext context;

    List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public KaiInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method)) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                log.debug(filter.getClass().getCanonicalName() + " ===> preFilter: " + preResult);
                return preResult;
            }
        }

        List<InstanceMeta> instances = context.getRouter().choose(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.debug("loadBalancer.choose(urls) ===> " + instance);

        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

        Object result = castReturnResult(method, rpcResponse);

        for (Filter filter : this.context.getFilters()) {
            Object postResult = filter.postFilter(rpcRequest, rpcResponse, result);
            if (postResult != null) {
                return postResult;
            }
        }

        return castReturnResult(method, rpcResponse);
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            return castMethodResult(method, rpcResponse.getData());
        } else {
            Exception exception = rpcResponse.getEx();
            if (exception instanceof KaiRpcException e) {
                throw e;
            } else {
                throw new KaiRpcException(rpcResponse.getEx(), KaiRpcException.UNKNOWN);
            }
        }
    }

    private static Object castMethodResult(Method method, Object result) {
        if (result instanceof JSONObject jsonResult) {
            return jsonResult.toJavaObject(method.getReturnType());
        } else {
            return TypeUtils.cast(result, method.getReturnType());
        }
    }

}
