package com.kai.kairpc.core.transport;

import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transport for springboot endpoint.
 */
@RestController
public class SpringBootTransport {

    @Autowired
    ProviderInvoker providerInvoker;

    // 使用 HTTP + JSON 实现通信和序列化
    @RequestMapping("/kairpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }
}
