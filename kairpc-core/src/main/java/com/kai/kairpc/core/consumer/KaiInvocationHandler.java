package com.kai.kairpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KaiInvocationHandler implements InvocationHandler {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    RpcContext context;

    List<String> providers;

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

        RpcResponse rpcResponse = post(rpcRequest, url);

        if (rpcResponse.isStatus()) {
            Object result = rpcResponse.getData();
            if (result instanceof JSONObject) {
                JSONObject jsonResult = (JSONObject) rpcResponse.getData();
                return jsonResult.toJavaObject(method.getReturnType());
            } else {
                return TypeUtils.cast(result, method.getReturnType());
            }
        } else {
            throw new RuntimeException(rpcResponse.getEx());
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String requestJson = JSON.toJSONString(rpcRequest);
        System.out.println("requestJson ===> " + requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson, JSON_TYPE))
                .build();
        try {
            String responseJson = client.newCall(request).execute().body().string();
            System.out.println("responseJson ===> " + responseJson);
            return JSON.parseObject(responseJson, RpcResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
