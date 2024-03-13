package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

// implements ApplicationContextAware 是为了 Spring 启动的时候，set applicationContext
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct // init-method，此时所有的 Bean 对象都已经创建好了（new 出来了），但是有可能没有初始化完成
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(KaiProvider.class);
        providers.forEach((k, v) -> System.out.println(k));

        providers.values().forEach(this::registerProvider);
    }

    public RpcResponse invoke(RpcRequest request) {
        // TODO: 过滤
        String methodName = request.getMethod();
        if ("toString".equals(methodName) || "hashCode".equals(methodName)) {
            return null;
        }

        RpcResponse rpcResponse = new RpcResponse();
        Object service = skeleton.get(request.getService());
        try {
            Method method = findMethod(service.getClass(), request.getMethod());
            Object result = method.invoke(service, request.getArgs());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (Exception e) {
            rpcResponse.setEx(new RuntimeException(e.getCause().getMessage()));
        }
        return rpcResponse;
    }

    private void registerProvider(Object object) {
        Class<?> anInterface = object.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), object);
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

}
