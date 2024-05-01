package com.kai.kairpc.core.api;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class RpcRequest {

    // 接口：com.kai.kairpc.demo.api.UserService
    private String service;
    // 方法：findById
    private String methodSign;
    // 参数：100
    private Object[] args;
    // 跨调用参数 consumer -> service1 -> service2 -> ...
    private Map<String, String> params = new HashMap<>();
}
