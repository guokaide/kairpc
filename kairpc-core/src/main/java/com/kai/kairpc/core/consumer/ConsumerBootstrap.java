package com.kai.kairpc.core.consumer;

import com.kai.kairpc.core.annotation.KaiConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    // 在所有的 Bean 初始化之后进行调用
    // 在 @KaiConsumer 标记的属性所在的 Bean 中更新该属性的值为代理类
    public void start() {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = findAnnotatedField(bean.getClass());
            fields.forEach(f -> {
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createConsumer(service);
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

    private Object createConsumer(Class<?> service) {
        return Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service},
                new KaiInvocationHandler(service)
        );
    }

    private List<Field> findAnnotatedField(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(KaiConsumer.class)) {
                    result.add(f);
                }
            }
            // Spring 中的 Bean 的类及其中包含的属性都可能是代理过的，如：
            // com.kai.kairpc.demo.consumer.KairpcDemoConsumerApplication$$SpringCGLIB$$0@6993c8df
            // 所以，我们还需要扫描其父类
            clazz = clazz.getSuperclass();
        }
        return result;
    }

}