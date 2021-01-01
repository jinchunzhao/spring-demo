package com.jy.ioc.context;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过xml的方式创建容器
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-01 21:50
 */
public class JyClassPathXmlApplicationContext {
    /**
     * 用来存储bean
     */
    private static final Map<String,Object> singletonObjects = new ConcurrentHashMap<>(256);
}
