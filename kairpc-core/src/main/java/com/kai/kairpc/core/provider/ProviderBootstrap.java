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

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct // init-method
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(KaiProvider.class);
        providers.forEach((k, v) -> System.out.println(k));

        providers.values().forEach(this::registerProvider);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object service = skeleton.get(request.getService());
        Method method = findMethod(service.getClass(), request.getMethod());
        try {
            Object result = method.invoke(service, request.getArgs());
            return new RpcResponse(true, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerProvider(Object object) {
        Class<?> anInterface = object.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getName(), object);
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
