package com.kai.kairpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.consumer.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;

    public OkHttpInvoker(int readTimeoutMs, int writeTimeoutMs, int connectTimeoutMs) {
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String requestJson = JSON.toJSONString(rpcRequest);
        log.debug("requestJson ===> " + requestJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson, JSON_TYPE))
                .build();
        try {
            try (Response response = client.newCall(request).execute()) {
                String responseJson = response.body().string();
                log.debug("responseJson ===> " + responseJson);
                return JSON.parseObject(responseJson, RpcResponse.class);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
