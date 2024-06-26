package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.core.annotation.EnableRpc;
import com.kai.kairpc.core.annotation.KaiConsumer;
import com.kai.kairpc.core.api.Router;
import com.kai.kairpc.core.api.RpcContext;
import com.kai.kairpc.core.cluster.GrayRouter;
import com.kai.kairpc.core.config.ConsumerConfig;
import com.kai.kairpc.demo.api.User;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
@Import(ConsumerConfig.class)
//@EnableRpc
public class KairpcDemoConsumerApplication {

    @KaiConsumer
    UserService userService;

    @Autowired
    UserAppService userAppService;

    public static void main(String[] args) {
        SpringApplication.run(KairpcDemoConsumerApplication.class, args);
    }

    @RequestMapping("/")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @RequestMapping("/find")
    public User find(@RequestParam("timeout") int timeout) {
        return userService.find(timeout);
    }

    @Autowired
    Router grayRouter;

    @RequestMapping("/gray")
    public String gray(@RequestParam("ratio") int ratio) {
        ((GrayRouter) grayRouter).setGrayRatio(ratio);
        return "OK: new ratio is " + ratio;
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return x -> testAll();
    }

    private void testAll() {
        // case: int -> User
        System.out.println("case 1: ==> [常规 int 类型，返回 User 对象]");
        User user = userService.findById(1);
        System.out.println("userService.findById(1) = " + user);
        System.out.println();

        // case: override
        System.out.println("case 2: ==> [测试方法重载，同名方法，参数不同]");
        User userKai = userService.findById(1, "kai");
        System.out.println("userService.findById(1, \"kai\") = " + userKai);
        System.out.println();

        // case: void -> String
        System.out.println("case 3: ==> [测试返回字符串]");
        System.out.println("userService.getName() = " + userService.getName());
        System.out.println();

        // case: override
        System.out.println("case 4: ==> [测试重载方法返回字符串]");
        System.out.println("userService.getName(123) = " + userService.getName(123));
        System.out.println();

        // case: local toString()
        System.out.println("case 5: ==> [测试 local toString() 方法]");
        System.out.println("userService.toString() = " + userService.toString());
        System.out.println();

        // case: 输入基本类型
        System.out.println("case 6: ==> [常规 int 类型，返回 User 对象]");
        System.out.println("userService.getId(10) = " + userService.getId(10));
        System.out.println();

        System.out.println("case 7: ==> [测试参数是 long+float 类型]");
        System.out.println("userService.getId(10L) = " + userService.getId(10L));
        System.out.println("userService.getId(10f) = " + userService.getId(10f));
        System.out.println();

        // case: 输入对象（User: fastjson 会将其转换为 LinkedHashMap）
        System.out.println("case 8: ==> [测试参数是 User 类型]");
        System.out.println("userService.getId(new User(100, \"Kai\") = " + userService.getId(new User(100, "Kai")));
        System.out.println();

        // case: 返回数组
        System.out.println("case 9: ==> [测试返回int[]+long[]]");
        System.out.println("Arrays.toString(userService.getIds() = " + Arrays.toString(userService.getIds()));
        System.out.println("Arrays.toString(userService.getLongIds()) = " + Arrays.toString(userService.getLongIds()));
        System.out.println();

        // case: 输入数组
        System.out.println("case 10: ==> [测试参数 int[], 返回 long[]]");
        System.out.println("Arrays.toString(userService.getIds(new int[]{4, 5, 6})) = " +
                Arrays.toString(userService.getIds(new int[]{4, 5, 6})));
        System.out.println();

        // case: 输入 List、输出 List（处理泛型）
        System.out.println("case 11: ==> [测试参数和返回值都是 List 类型]");
        System.out.println("userService.getList(List.of(\n" +
                "                new User(100, \"Kai100\"),\n" +
                "                new User(101, \"Kai101\")\n" +
                ")) = " +
                userService.getList(List.of(
                        new User(100, "Kai100"),
                        new User(101, "Kai101")
                )));
        System.out.println();

        // case: 输入 Map、输出 Map
        System.out.println("case 12: ==> [测试参数和返回值都是 Map 类型]");
        Map<String, User> map = new HashMap<>();
        map.put("M200", new User(200, "Kai200"));
        map.put("M201", new User(201, "Kai201"));
        System.out.println("userService.getMap(map) = " + userService.getMap(map));
        System.out.println();

        System.out.println("case 13: ==> [测试参数和返回值都是 Boolean/boolean 类型]");
        System.out.println(userService.getFlag(false));
        System.out.println();

        System.out.println("case 14: ==> [测试参数和返回值都是 User[] 类型]");
        System.out.println(Arrays.toString(userService.getUsers(new User[]{
                new User(140, "case14"),
                new User(141, "case14")
        })));
        System.out.println();

        System.out.println("case 15: ==> [测试参数为 long, 返回值为 User]");
        System.out.println(userService.findById(1000L));
        System.out.println();

        System.out.println("case 16: ==> [测试参数为 boolean, 返回值为 User]");
        System.out.println(userService.ex(false));
        System.out.println();

        System.out.println("case 17: ==> [测试服务端抛出一个 RuntimeException 异常]");
        try {
            User ex = userService.ex(true);
            System.out.println(ex);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();

        System.out.println("case 18: ==> [测试服务端抛出一个超时重试后成功的场景]");
        // 超时重试的【漏斗原则】
        // A 2000 -> B 1500 -> C 1200 -> 1000
        long start = System.currentTimeMillis();
        userService.find(1100);
        userService.find(1100);
        System.out.println("userService.find(1100) take " + (System.currentTimeMillis() - start) + "ms");
        System.out.println();

        System.out.println("case 19: ==> [测试通过 Context 跨消费者和提供者传参]");
        String KeyVersion = "rpc.version";
        RpcContext.setContextParameter(KeyVersion, "v1");
        System.out.println("c -> p -> c: userService.echoParameter(\"" + KeyVersion + "\") = " + userService.echoParameter(KeyVersion));

        String keyMessage = "rpc.message";
        RpcContext.setContextParameter(keyMessage, "this is a test message");
        System.out.println("c -> p -> c: userService.echoParameter(\"" + keyMessage + "\") = " + userService.echoParameter(keyMessage));
    }
}
