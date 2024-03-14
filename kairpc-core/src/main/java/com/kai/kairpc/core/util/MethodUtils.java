package com.kai.kairpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

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

    // for test
    public static String methodSign(Method method, Class<?> clz) {
        return null;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(m -> {
            System.out.println(methodSign(m));
        });
    }
}
