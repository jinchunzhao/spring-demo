package com.jy.ioc.service.impl;

import com.jy.ioc.service.UserService;

/**
 * UserServiceImpl
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-01 21:39
 */
public class UserServiceImpl implements UserService {
    @Override
    public String getUserName(String userName) {
        System.out.println("UserServiceImpl 实例化了");
        return userName;
    }
}
