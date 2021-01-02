package com.jy.ioc.service;
/**
 * {@link UserService}
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-01 21:39
 */
public interface UserService {

    /**
     * 获取用户名
     *
     * @param userName 用户名
     * @return
     *        用户名
     */
    String getUserName(String userName);
}
