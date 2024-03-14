package com.kai.kairpc.demo.consumer;

import com.kai.kairpc.core.annotation.KaiConsumer;
import com.kai.kairpc.demo.api.User;
import com.kai.kairpc.demo.api.UserService;
import org.springframework.stereotype.Component;

@Component
public class UserAppService {

    @KaiConsumer
    UserService userService;

    public void test() {
        User user = userService.findById(100);
        System.out.println("test ===> " + user);
    }
}
