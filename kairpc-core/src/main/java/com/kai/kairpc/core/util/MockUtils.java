package com.kai.kairpc.core.util;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;

import java.lang.reflect.Field;

public class MockUtils {
    public static Object mock(Class<?> type) {
        if (type.isPrimitive()) {
            return mockPrimitive(type);
        }
        if (type.equals(String.class)) {
            return "Mock String";
        }
        return null;
    }

    private static Object mockPrimitive(Class<?> type) {
        if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.MAX_VALUE;
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.MAX_VALUE;
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.MAX_VALUE;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.MAX_VALUE;
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.MAX_VALUE;
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.MAX_VALUE;
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return Character.MAX_VALUE;
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.TRUE;
        }
        return null;
    }

    @SneakyThrows
    private static Object mockPoJo(Class<?> type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(result, mock(field.getType()));
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(mockPoJo(UserDto.class));
    }

    @Data
    @ToString
    public static class UserDto {
        private int id;
        private String name;
    }
}


