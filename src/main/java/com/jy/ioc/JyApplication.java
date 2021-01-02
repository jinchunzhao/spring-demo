package com.jy.ioc;

import com.jy.ioc.context.JyClassPathXmlApplicationContext;
import com.jy.ioc.service.impl.UserServiceImpl;

/**
 * 入口主类
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-02 20:31
 */
public class JyApplication {


    public static void main(String[] args) throws Exception{
        JyClassPathXmlApplicationContext applicationContext = new JyClassPathXmlApplicationContext("classpath:applicationContext.xml");
        UserServiceImpl userService = (UserServiceImpl) applicationContext.getBean("userService");
        userService.getUserName("hello word");
    }
}
