package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.meta.ProviderMeta;
import com.kai.kairpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// implements ApplicationContextAware 是为了 Spring 启动的时候，set applicationContext
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    // <InterfaceName, List<ProviderMeta>>
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct // init-method，此时所有的 Bean 对象都已经创建好了（new 出来了），但是有可能没有初始化完成
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(KaiProvider.class);
        providers.forEach((k, v) -> System.out.println(k));

        providers.values().forEach(this::registerProvider);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object result = method.invoke(meta.getServiceImpl(), request.getArgs());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (Exception e) {
            rpcResponse.setEx(new RuntimeException(e.getCause().getMessage()));
        }
        return rpcResponse;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }

    // 可以将服务提供方提供的实现类的方法签名全部都缓存起来，原因是：
    // 1. 服务提供方提供的方法是有限的，即使全部缓存起来也没有问题
    // 2. 如果没有缓存，服务消费方每天调用，都需要计算方法签名的话，会影响性能
    private void registerProvider(Object object) {
        Class<?> anInterface = object.getClass().getInterfaces()[0];
        Method[] methods = anInterface.getMethods();
        for (Method method : methods) {
            if (MethodUtils.checkLocalMethod(method)) {
                continue;
            }
            createProvider(anInterface, object, method);
        }
    }

    private void createProvider(Class<?> anInterface, Object object, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setServiceImpl(object);
        meta.setMethodSign(MethodUtils.methodSign(method));
        System.out.println("create a provider: " + meta);
        skeleton.add(anInterface.getCanonicalName(), meta);
    }

}
