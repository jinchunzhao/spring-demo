package com.jy.ioc.service.impl;

import com.jy.ioc.annotation.JyComponent;
import com.jy.ioc.service.OrderService;

/**
 * 订单实现类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-02 23:00
 */
@JyComponent
public class OrderServiceImpl implements OrderService {
    @Override
    public String getOrderId(String orderId) {
        System.out.println("OrderServiceImpl实例创建了");
        return orderId;
    }
}
