package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.demo.api.User;
import com.kai.kairpc.demo.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public User findById(long id) {
        return new User((int) id, "Kai-" + System.currentTimeMillis());
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

    // java.lang.ClassCastException: class java.util.LinkedHashMap cannot be cast to class com.kai.kairpc.demo.api.User
    // 这个报错是因为没有 List<User> 的泛型丢失，变成了 List<LinkedHashMap>
    @Override
    public List<User> getList(List<User> users) {
        return users.stream().peek(x -> x.setName(x.getName() + "_" + System.currentTimeMillis())).collect(Collectors.toList());
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        userMap.forEach((k, v) -> v.setName(v.getName() + "_" + System.currentTimeMillis()));
        return userMap;
    }

    @Override
    public boolean getFlag(Boolean flag) {
        return flag;
    }

    @Override
    public User[] getUsers(User[] users) {
        return Arrays.stream(users).peek(x -> x.setName(x.getName() + "_" + System.currentTimeMillis())).toList().toArray(User[]::new);
    }

    @Override
    public User ex(boolean flag) {
        if (flag) {
            throw new RuntimeException("something is error");
        }
        return new User(0, "Kai-ex-0");
    }

    String timeoutPorts = "8081,8094";

    @Override
    public User find(int timeout) {
        String port = environment.getProperty("server.port");
        if (Arrays.asList(timeoutPorts.split(",")).contains(port)) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User(0, "Kai0-" + port);
    }

    @Override
    public void setTimeoutPorts(String ports) {
        timeoutPorts = ports;
    }
}
