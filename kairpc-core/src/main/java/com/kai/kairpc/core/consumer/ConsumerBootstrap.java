package com.kai.kairpc.core.consumer;

import com.kai.kairpc.core.annotation.KaiConsumer;
import com.kai.kairpc.core.api.LoadBalancer;
import com.kai.kairpc.core.api.RegistryCenter;
import com.kai.kairpc.core.api.Router;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.util.MethodUtils;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    // 在所有的 Bean 初始化之后进行调用
    // 在 @KaiConsumer 标记的属性所在的 Bean 中更新该属性的值为代理类
    public void start() {
        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = MethodUtils.findAnnotatedField(bean.getClass(), KaiConsumer.class);
            fields.forEach(f -> {
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createFromRegistry(service, context, rc);
                        this.stub.put(serviceName, consumer);
                    }
                    f.setAccessible(true);
                    f.set(bean, consumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> service, RpcContext context, RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        List<String> providers = mapToUrls(rc.fetchAll(serviceName));
        System.out.println(" ===> map to providers: " + providers);

        rc.subscribe(serviceName, event -> {
            providers.clear();
            providers.addAll(mapToUrls(event.getData()));
        });

        return createConsumer(service, context, providers);
    }

    private List<String> mapToUrls(List<String> nodes) {
        return nodes.stream().map(x -> "http://" + x.replace("_", ":")).collect(Collectors.toList());
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service},
                new KaiInvocationHandler(service, context, providers)
        );
    }


}
