package com.kai.kairpc.core.consumer;

import com.kai.kairpc.core.api.*;
import com.kai.kairpc.core.consumer.http.OkHttpInvoker;
import com.kai.kairpc.core.governance.SlidingTimeWindow;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务消费者动态代理处理类
 */
@Slf4j
public class KaiInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext context;

    final List<InstanceMeta> providers;

    final List<InstanceMeta> isolatedProviders = new ArrayList<>();

    final List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    final Map<String, SlidingTimeWindow> windows = new HashMap<>();

    HttpInvoker httpInvoker;

    ScheduledExecutorService executor;

    public KaiInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("consumer.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout, timeout, timeout);
        this.executor = Executors.newScheduledThreadPool(1);
        // 2. half open: 每分钟将隔离的节点放出来，对部分流量提供服务，测试其是否恢复
        int halfOpenInitialDelay = Integer.parseInt(context.getParameters().getOrDefault("consumer.halfOpenInitialDelay", "10000"));
        int halfOpenDelay = Integer.parseInt(context.getParameters().getOrDefault("consumer.halfOpenDelay", "60000"));
        this.executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (MethodUtils.checkLocalMethod(method)) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        int retries = Integer.parseInt(context.getParameters().getOrDefault("consumer.retries", "1"));
        int faultLimit = Integer.parseInt(context.getParameters().getOrDefault("consumer.faultLimit", "10"));

        while (retries-- > 0) {

            log.debug("retries left: " + retries);

            try {
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.preFilter(rpcRequest);
                    if (preResult != null) {
                        log.debug(filter.getClass().getCanonicalName() + " ===> preFilter: " + preResult);
                        return preResult;
                    }
                }

                InstanceMeta instance;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> instances = context.getRouter().choose(providers);
                        instance = context.getLoadBalancer().choose(instances);
                        log.debug("loadBalancer.choose(urls) ===> {}", instance);
                    } else {
                        // half open 探活
                        instance = halfOpenProviders.remove(0);
                        log.debug("check alive instance ===> {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse;
                Object result;

                String url = instance.toUrl();
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, url);
                    result = processResponse(method, rpcResponse);
                } catch (Exception e) {
                    // 1. 故障隔离：故障的规则统计和隔离
                    synchronized (windows) {
                        // 每发生一次异常，滑动窗口就记录一次（默认：统计 30s 的异常数）
                        SlidingTimeWindow window = windows.get(url);
                        if (window == null) {
                            window = new SlidingTimeWindow();
                            windows.put(url, window);
                        }
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in window with {}", url, window.getSum());
                        // 若 30s 内异常发生了 10 次，就将这个节点隔离，暂时不提供服务
                        if (window.getSum() >= faultLimit) {
                            isolate(instance);
                        }
                    }
                    throw e;
                }

                // 3. full open
                synchronized (providers) {
                    // 如果本次探活成功
                    if (!providers.contains(instance)) {
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolatedProviders={}, providers={}",
                                instance, isolatedProviders, providers);
                    }
                }

                for (Filter filter : this.context.getFilters()) {
                    Object postResult = filter.postFilter(rpcRequest, rpcResponse, result);
                    if (postResult != null) {
                        return postResult;
                    }
                }

                return processResponse(method, rpcResponse);
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw e;
                }
            }
        }
        return null;
    }

    private void halfOpen() {
        log.debug(" ===> half open isolatedProviders: " + isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
    }

    private void isolate(InstanceMeta instance) {
        log.debug(" ==> isolate instance: " + instance);
        providers.remove(instance);
        log.debug(" ==> providers = {}", providers);
        isolatedProviders.add(instance);
        log.debug(" ==> isolated providers = {}", isolatedProviders);
    }

    private static Object processResponse(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            return castResponseData(method, rpcResponse.getData());
        } else {
            RpcException exception = rpcResponse.getEx();
            if (exception != null) {
                log.error("response error", exception);
                throw exception;
            }
            return null;
        }
    }

    private static Object castResponseData(Method method, Object data) {
        Class<?> type = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        return TypeUtils.cast(data, type, genericReturnType);
    }

}
