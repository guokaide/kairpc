package com.kai.kairpc.demo.api;

/**
 * 服务契约
 */
public interface UserService {
    User findById(int id);

    // 返回基本类型
    int getId(int id);

    String getName();
}
