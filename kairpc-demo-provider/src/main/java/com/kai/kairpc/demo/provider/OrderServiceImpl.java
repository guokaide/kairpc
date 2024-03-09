package com.kai.kairpc.demo.provider;

import com.kai.kairpc.core.annotation.KaiProvider;
import com.kai.kairpc.demo.api.Order;
import com.kai.kairpc.demo.api.OrderService;
import org.springframework.stereotype.Service;

@Service
@KaiProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {
        return new Order(Long.valueOf(id), 15.6f);
    }
}
