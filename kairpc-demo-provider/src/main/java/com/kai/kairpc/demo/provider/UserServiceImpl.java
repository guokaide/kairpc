package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.demo.api.User;
import com.kai.kairpc.demo.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@KaiProvider
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "Kai-"
                + environment.getProperty("server.port")
                + "_" + System.currentTimeMillis());
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
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(float id) {
        return (long) id;
    }

    @Override
    public long getId(User user) {
        return user.getId();
    }

    @Override
    public int[] getIds() {
        return new int[]{1, 2, 3};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{100, 200, 300};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
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
