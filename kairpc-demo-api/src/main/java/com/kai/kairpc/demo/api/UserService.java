package com.kai.kairpc.demo.api;

/**
 * 服务契约
 */
public interface UserService {
    User findById(int id);

    User findById(int id, String name);

    int getId(int id);

    long getId(long id);

    long getId(float id);

    long getId(User user);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    String getName();

    String getName(int id);
}
