package com.kai.kairpc.demo.api;

import java.util.List;
import java.util.Map;

/**
 * 服务契约
 */
public interface UserService {
    User findById(int id);

    User findById(int id, String name);

    User findById(long id);

    int getId(int id);

    long getId(long id);

    long getId(float id);

    long getId(User user);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    String getName();

    String getName(int id);

    List<User> getList(List<User>  users);

    Map<String, User> getMap(Map<String, User> userMap);

    boolean getFlag(Boolean flag);

    User[] getUsers(User[] users);

    User ex(boolean flag);

    User find(int timeout);

    void setTimeoutPorts(String ports);

    String echoParameter(String key);
}
