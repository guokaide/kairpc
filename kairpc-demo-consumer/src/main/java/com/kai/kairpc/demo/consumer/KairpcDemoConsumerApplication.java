package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.core.annotation.KaiConsumer;
import com.kai.kairpc.core.consumer.ConsumerConfig;
import com.kai.kairpc.demo.api.OrderService;
import com.kai.kairpc.demo.api.User;
import com.kai.kairpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@Import(ConsumerConfig.class)
public class KairpcDemoConsumerApplication {

    @KaiConsumer
    UserService userService;

    @KaiConsumer
    OrderService orderService;

    @Autowired
    UserAppService userAppService;

    public static void main(String[] args) {
        SpringApplication.run(KairpcDemoConsumerApplication.class, args);
    }

    @RequestMapping("/")
    public User findById(int id) {
        return userService.findById(id);
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return x -> {
//            System.out.println(userService.getId(10));
            // TODO: 待修复类型问题
//            System.out.println("RPC result userService.getId(new User(100, \"Kai\") = " +
//                    userService.getId(new User(100, "Kai")));


//            User user = userService.findById(1);
//            System.out.println("RPC result userService.findById(1) = " + user);
//
//            User userKai = userService.findById(1, "kai");
//            System.out.println("RPC result userService.findById(1, \"kai\") = " + userKai);
//
//            System.out.println(userService.getName());
//
//            System.out.println(userService.getName(123));

//            Order order = orderService.findById(2);
//            // 测试异常
////            Order order = orderService.findById(404);
//            System.out.println(order);
//
//            // 基本类型
//            System.out.println(userService.getId(100));
//            System.out.println(userService.getName());
//
//            userAppService.test();
        };
    }
}
