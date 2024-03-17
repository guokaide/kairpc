package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.core.annotation.KaiConsumer;
import com.kai.kairpc.core.consumer.ConsumerConfig;
import com.kai.kairpc.demo.api.Order;
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

import java.util.Arrays;

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
            // case: int -> User
            User user = userService.findById(1);
            System.out.println("userService.findById(1) = " + user);

            // case: override
            User userKai = userService.findById(1, "kai");
            System.out.println("userService.findById(1, \"kai\") = " + userKai);

            // case: void -> String
            System.out.println(userService.getName());

            // case: override
            System.out.println(userService.getName(123));


            // case: 输入基本类型
            System.out.println(userService.getId(100));

            System.out.println(userService.getId(100L));

            System.out.println("userService.getId(10f) = " + userService.getId(10f));

            // case: 输入对象（User: fastjson 会将其转换为 LinkedHashMap）
            System.out.println("userService.getId(new User(100, \"Kai\") = " + userService.getId(new User(100, "Kai")));

            // case: 返回数组
            System.out.println(Arrays.toString(userService.getIds()));

            System.out.println(Arrays.toString(userService.getLongIds()));

            // case: 输入数组
            System.out.println(Arrays.toString(userService.getIds(new int[]{4, 5, 6})));

            // 测试 @KaiConsumer 在其他
            userAppService.test();

            System.out.println(orderService.findById(2));
            // 测试异常
//            Order order = orderService.findById(404);
//            System.out.println(order);
        };
    }
}
