package com.kai.kairpc.core.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.config.AppProperties;
import com.kai.kairpc.core.config.ProviderProperties;
import com.kai.kairpc.core.meta.InstanceMeta;
import com.kai.kairpc.core.meta.ProviderMeta;
import com.kai.kairpc.core.meta.ServiceMeta;
import com.kai.kairpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 服务提供者启动类
 * implements ApplicationContextAware 是为了 Spring 启动的时候，set applicationContext
 */
@Slf4j
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    RegistryCenter registryCenter;

    private String port;

    private AppProperties appProperties;

    private ProviderProperties providerProperties;

    // <InterfaceName, List<ProviderMeta>>
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    private InstanceMeta instance;

    public ProviderBootstrap(String port, AppProperties appProperties, ProviderProperties providerProperties) {
        this.port = port;
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    @PostConstruct // init-method，此时所有的 Bean 对象都已经创建好了（new 出来了），但是有可能没有初始化完成
    public void init() {
        registryCenter = applicationContext.getBean(RegistryCenter.class);
        // <beanName, 接口实现类>
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(KaiProvider.class);
        providers.forEach((k, v) -> log.info(k));

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
        this.instance = InstanceMeta.http(ip, Integer.valueOf(port)).addParams(providerProperties.getMetas());

        registryCenter.start();
        // 服务注册：将提供的服务注册到注册中心
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        registryCenter.stop();
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getApp()).namespace(appProperties.getNamespace())
                .env(appProperties.getEnv()).name(service).build();
        registryCenter.unregister(serviceMeta, instance);
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getApp()).namespace(appProperties.getNamespace())
                .env(appProperties.getEnv()).name(service).build();
        registryCenter.register(serviceMeta, instance);
    }

    // 可以将服务提供方提供的实现类的方法签名全部都缓存起来，原因是：
    // 1. 服务提供方提供的方法是有限的，即使全部缓存起来也没有问题
    // 2. 如果没有缓存，服务消费方每次调用，都需要计算方法签名的话，会影响性能
    private void registerProvider(Object impl) {
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> {
                    Arrays.stream(service.getMethods())
                            .filter(method -> !MethodUtils.checkLocalMethod(method))
                            .forEach(method -> createProvider(service, impl, method));
                });
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder()
                .method(method).serviceImpl(impl).methodSign(MethodUtils.methodSign(method)).build();
        log.info("create a provider: " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }

}
