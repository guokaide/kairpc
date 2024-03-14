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

    @Override
    public User findById(int id, String name) {
        return new User(id, "Kai-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public int getId(int id) {
        return id;
    }

    @Override
    public String getName() {
        return "Kai123";
    }

    @Override
    public String getName(int id) {
        return "Cola-" + id;
    }
}
