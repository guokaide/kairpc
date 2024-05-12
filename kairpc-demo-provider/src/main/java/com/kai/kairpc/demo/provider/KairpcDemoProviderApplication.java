package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.api.RpcException;
import com.kai.kairpc.core.api.RpcRequest;
import com.kai.kairpc.core.api.RpcResponse;
import com.kai.kairpc.core.config.ProviderConfig;
import com.kai.kairpc.core.transport.SpringBootTransport;
import com.kai.kairpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
//@EnableRpc
public class KairpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(KairpcDemoProviderApplication.class, args);
    }


    @Autowired
    SpringBootTransport transport;

    @Autowired
    UserService userService;

    @RequestMapping("/ports")
    public RpcResponse<Object> invoke(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<Object> response = new RpcResponse<>();
        response.setData("OK:" + ports);
        response.setStatus(true);
        return response;
    }

    @Bean
    ApplicationRunner applicationRunner() {
        return x -> {
            // 1. test 1 parameter
            RpcRequest request = new RpcRequest();
            request.setService("com.kai.kairpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});
            RpcResponse<Object> response = transport.invoke(request);
            System.out.println("return : " + response.getData());

            // 2. test 2 parameters
            RpcRequest request1 = new RpcRequest();
            request1.setService("com.kai.kairpc.demo.api.UserService");
            request1.setMethodSign("findById@2_int_java.lang.String");
            request1.setArgs(new Object[]{100, "Kai"});
            RpcResponse<Object> response1 = transport.invoke(request);
            System.out.println("return : " + response1.getData());

            // 3. test traffic control
//            RpcRequest request2 = new RpcRequest();
//            request2.setService("com.kai.kairpc.demo.api.UserService");
//            request2.setMethodSign("findById@1_int");
//            request2.setArgs(new Object[]{100});
//            for (int i = 0; i < 100; i++) {
//                Thread.sleep(1000);
//                try {
//                    RpcResponse<Object> response2 = transport.invoke(request2);
//                    System.out.println(i + " >>> " + response2.getData());
//                } catch (RpcException e) {
//                    System.out.println(i + " >>> " + e.getMessage() + " -> " + e.getErrCode());
//                }
//            }
        };
    }

}
