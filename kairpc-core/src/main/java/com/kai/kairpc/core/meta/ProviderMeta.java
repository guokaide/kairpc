package com.kai.kairpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
public class ProviderMeta {
    Method method;
    String methodSign;
    Object serviceImpl;
}
