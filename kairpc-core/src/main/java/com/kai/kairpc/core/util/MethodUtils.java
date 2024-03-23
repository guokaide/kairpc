package com.kai.kairpc.core.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MethodUtils {

    public static boolean checkLocalMethod(final String method) {
        return "toString".equals(method) ||
                "equals".equals(method) ||
                "hashCode".equals(method) ||
                "getClass".equals(method) ||
                "wait".equals(method) ||
                "notify".equals(method) ||
                "notifyAll".equals(method);
    }

    public static boolean checkLocalMethod(final Method method) {
        return Object.class.equals(method.getDeclaringClass());
    }

    public static String methodSign(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(p -> {
            sb.append("_").append(p.getCanonicalName());
        });
        return sb.toString();
    }

    public static List<Field> findAnnotatedField(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> result = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(annotation)) {
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

    // for test
    public static String methodSign(Method method, Class<?> clz) {
        return null;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(m -> {
            log.info(methodSign(m));
        });
    }
}
