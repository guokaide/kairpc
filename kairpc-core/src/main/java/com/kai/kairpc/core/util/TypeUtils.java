package com.kai.kairpc.core.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TypeUtils {

    public static Object cast(Object data, Class<?> type, Type genericType) {
        if (data == null) {
            return null;
        }

        if (data instanceof List<?> list) {
            return castList(list, type, genericType);
        }

        if (data instanceof Map<?, ?> map) {
            return castMap(map, type, genericType);
        }

        return cast(data, type);
    }

    private static Object castMap(Map<?, ?> data, Class<?> type, Type genericType) {
        if (Map.class.isAssignableFrom(type)) {
            if (data instanceof JSONObject jsonObject) { // data 是 pojo, type 是 map
                log.debug(" ====> JSONObject(subclass of map) -> POJO");
                return jsonObject.toJavaObject(type);
            } else { // data 是 map, type 是 map
                log.debug(" ====> map(or subclass of map) -> map");
                Map<Object, Object> result = new HashMap<>();
                if (genericType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                    data.forEach((k, v) -> result.put(cast(k, keyType), cast(v, valueType)));
                } else {
                    result.putAll(data);
                }
                return result;
            }
        } else if (!Map.class.isAssignableFrom(type)) { // data 是 map, type 不是 map，是 pojo
            log.debug(" ====> map -> POJO");
            return new JSONObject((Map<String, Object>) data).toJavaObject(type);
        }
        log.debug(" ====> map -> ?(map)");
        return data;
    }

    private static Object castList(List<?> data, Class<?> type, Type genericType) {
        if (type.isArray()) {
            log.debug(" ====> list -> array");
            Class<?> componentType = type.getComponentType();
            Object[] array = data.toArray();
            Object result = Array.newInstance(componentType, array.length);
            for (int i = 0; i < array.length; i++) {
                if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                    Array.set(result, i, array[i]);
                } else {
                    Array.set(result, i, cast(array[i], componentType));
                }
            }
            return result;
        }

        if (List.class.isAssignableFrom(type)) {
            log.debug(" ====> list(or subclass of list) -> list");
            List<Object> result = new ArrayList<>(data.size());
            if (genericType instanceof ParameterizedType parameterizedType) {
                Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                data.forEach(x -> result.add(cast(x, actualTypeArgument)));
            } else {
                result.addAll(data);
            }
            return result;
        }
        return null;
    }

    private static Object cast(Object origin, Class<?> type) {
        if (origin == null) {
            return null;
        }

        if (type.isAssignableFrom(origin.getClass())) {
            return origin;
        }

        if (origin instanceof Map<?, ?> map) {
            JSONObject jsonObject = new JSONObject((Map<String, Object>) map);
            return jsonObject.toJavaObject(type);
        }

        if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return origin.toString().charAt(0);
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return origin;
        }
        throw new IllegalArgumentException("Unsupported type: " + type.getName() + ", value: " + origin);
    }

}
