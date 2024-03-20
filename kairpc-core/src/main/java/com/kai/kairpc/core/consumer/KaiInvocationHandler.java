package com.kai.kairpc.core.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.consumer.http.OkHttpInvoker;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务消费者动态代理处理类
 */
public class KaiInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext context;

    List<String> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public KaiInvocationHandler(Class<?> clazz, RpcContext context, List<String> providers) {
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

        List<String> urls = context.getRouter().choose(providers);
        String url = (String) context.getLoadBalancer().choose(urls);
        System.out.println("loadBalancer.choose(urls) ===> " + url);

        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, url);

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
