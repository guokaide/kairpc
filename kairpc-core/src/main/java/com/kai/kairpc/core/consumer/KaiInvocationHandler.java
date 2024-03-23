package com.kai.kairpc.core.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.consumer.http.OkHttpInvoker;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

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

        List<InstanceMeta> instances = context.getRouter().choose(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.debug("loadBalancer.choose(urls) ===> " + instance);

        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

        if (rpcResponse.isStatus()) {
            Object result = rpcResponse.getData();
            return castMethodResult(method, result);
        } else {
            throw new RuntimeException(rpcResponse.getEx());
        }
    }

    @Nullable
    private static Object castMethodResult(Method method, Object result) {
        if (result instanceof JSONObject jsonResult) {
            return jsonResult.toJavaObject(method.getReturnType());
        } else {
            return TypeUtils.cast(result, method.getReturnType());
        }
    }

}
