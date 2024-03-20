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
            System.out.println("case 1: ==> [常规 int 类型，返回 User 对象]");
            User user = userService.findById(1);
            System.out.println("userService.findById(1) = " + user);

            // case: override
            System.out.println("case 2: ==> [测试方法重载，同名方法，参数不同]");
            User userKai = userService.findById(1, "kai");
            System.out.println("userService.findById(1, \"kai\") = " + userKai);

            // case: void -> String
            System.out.println("case 3: ==> [测试返回字符串]");
            System.out.println("userService.getName() = " + userService.getName());

            // case: override
            System.out.println("case 4: ==> [测试重载方法返回字符串]");
            System.out.println("userService.getName(123) = " + userService.getName(123));

            // case: local toString()
            System.out.println("case 5: ==> [测试 local toString() 方法]");
            System.out.println("userService.toString() = " + userService.toString());

            // case: 输入基本类型
            System.out.println("case 6: ==> [常规 int 类型，返回 User 对象]");
            System.out.println(userService.getId(10));

            System.out.println("case 7: ==> [测试 long+float 类型]");
            System.out.println("userService.getId(100L) = " + userService.getId(10L));
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
