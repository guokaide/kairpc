package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.provider.ProviderBootstrap;
import com.kai.kairpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class KairpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(KairpcDemoProviderApplication.class, args);
    }


    @Autowired
    ProviderBootstrap providerBootstrap;

    // 使用 HTTP + JSON 实现通信和序列化
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }

    @Bean
    ApplicationRunner applicationRunner() {
        return x -> {
            // 1. test 1 parameter
            RpcRequest request = new RpcRequest();
            request.setService("com.kai.kairpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});
            RpcResponse response = invoke(request);
            System.out.println("return : " + response.getData());

            // 2. test 2 parameters
            RpcRequest request1 = new RpcRequest();
            request1.setService("com.kai.kairpc.demo.api.UserService");
            request1.setMethodSign("findById@2_int_java.lang.String");
            request1.setArgs(new Object[]{100, "Kai"});
            RpcResponse response1 = invoke(request);
            System.out.println("return : " + response1.getData());
        };
    }

}
