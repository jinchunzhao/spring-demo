package com.jy.ioc;

import com.jy.ioc.context.JyAnnotationApplicationContext;
import com.jy.ioc.context.JyClassPathXmlApplicationContext;
import com.jy.ioc.service.impl.OrderServiceImpl;
import com.jy.ioc.service.impl.UserServiceImpl;

/**
 * 入口主类
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-02 21:44
 */
public class JyApplication {


    public static void main(String[] args) throws Exception{
        JyClassPathXmlApplicationContext applicationContext = new JyClassPathXmlApplicationContext("classpath:applicationContext.xml");
        UserServiceImpl userService = (UserServiceImpl) applicationContext.getBean("userService");
        String userName = userService.getUserName("hello word");
        System.out.println(userName);

        JyAnnotationApplicationContext applicationContext1 = new JyAnnotationApplicationContext();
        OrderServiceImpl orderService = (OrderServiceImpl) applicationContext1.getBean("orderService");
        String orderId = orderService.getOrderId("12124124124");
        System.out.println(orderId);
    }
}
