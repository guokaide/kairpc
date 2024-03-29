package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.meta.ProviderMeta;
import com.kai.kairpc.core.util.MethodUtils;
import com.kai.kairpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 服务提供者启动类
 * implements ApplicationContextAware 是为了 Spring 启动的时候，set applicationContext
 */
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    // <InterfaceName, List<ProviderMeta>>
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private String instance;

    @Value("${server.port}")
    private String port;

    @PostConstruct // init-method，此时所有的 Bean 对象都已经创建好了（new 出来了），但是有可能没有初始化完成
    public void init() {
        // <beanName, 接口实现类>
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(KaiProvider.class);
        providers.forEach((k, v) -> System.out.println(k));

        // 本地注册：将提供的服务暴露出去
        providers.values().forEach(this::registerProvider);

//        String ip = InetAddress.getLocalHost().getHostAddress();
//        this.instance = ip + "_" + port;

        // 服务注册：将提供的服务注册到注册中心
        // 此时，zk 上已经有这些服务了，但是 Spring 还没有初始化好所有的 Bean，
        // 所以会导致服务没有准备好就已经暴露出去了，服务消费者调用会报错
        // skeleton.keySet().forEach(this::registerService);
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        this.instance = ip + "_" + port;

        // 服务注册：将提供的服务注册到注册中心
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
    }

    private void unregisterService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.unregister(service, instance);
    }

    private void registerService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.register(service, instance);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (Exception e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            rpcResponse.setEx(new RuntimeException(message));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] actualArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArgs[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actualArgs;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }

    // 可以将服务提供方提供的实现类的方法签名全部都缓存起来，原因是：
    // 1. 服务提供方提供的方法是有限的，即使全部缓存起来也没有问题
    // 2. 如果没有缓存，服务消费方每次调用，都需要计算方法签名的话，会影响性能
    private void registerProvider(Object object) {
        Arrays.stream(object.getClass().getInterfaces()).forEach(anInterface -> {
            Method[] methods = anInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(anInterface, object, method);
            }
        });
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
