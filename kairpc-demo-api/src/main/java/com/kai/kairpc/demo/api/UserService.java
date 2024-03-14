package com.kai.kairpc.demo.api;

/**
 * 服务契约
 */
public interface UserService {
    User findById(int id);

    User findById(int id, String name);

    // 返回基本类型
    int getId(int id);

    String getName();

    String getName(int id);
}
