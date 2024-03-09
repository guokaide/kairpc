package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.demo.api.User;
import com.kai.kairpc.demo.api.UserService;
import org.springframework.stereotype.Service;

@Service
@KaiProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(int id) {
        return new User(id, "Kai-" + System.currentTimeMillis());
    }
}
