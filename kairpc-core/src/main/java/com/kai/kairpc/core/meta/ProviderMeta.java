package com.kai.kairpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class ProviderMeta {
    Method method;
    String methodSign;
    Object serviceImpl;
}
