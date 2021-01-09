package com.jy.ioc.context;

import com.jy.ioc.JyApplication;
import com.jy.ioc.annotation.JyComponent;
import com.jy.ioc.utils.ClassUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过注解的方式创建容器
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-02 22:46
 */
public class JyAnnotationApplicationContext {
    /**
     * 用来存储bean
     */
    private static final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * 加载bean
     *
     * @param beanId
     * @return
     * @throws Exception
     */
    public Object getBean(String beanId)throws Exception{
        if (singletonObjects.containsKey(beanId)){
            return singletonObjects;
        }else {
            return doCreateBean(beanId);
        }
    }

    /**
     * 创建bean
     *
     * @param beanId
     * @return
     * @throws Exception
     */
    private Object doCreateBean(String beanId) throws Exception{
        //扫JyApplication下的所有包
        List<Class<?>> classes = ClassUtil.getAllClassByPackageName(JyApplication.class.getPackage());
        Object obj = null;
        for (Class< ? > clazz : classes) {
            //判断是否有自定义的注解
            JyComponent annotation = clazz.getAnnotation(JyComponent.class);
            if (Objects.isNull(annotation)){
                continue;
            }
            //有则实例化
            String name = clazz.getSimpleName();
            String annotationBeanId = name.substring(0, 1).toLowerCase() + name.substring(1);
            Object instance = clazz.newInstance();
            if (Objects.equals(beanId,annotationBeanId)){
                obj = instance;
            }
            singletonObjects.put(beanId,instance);
        }
        return obj;
    }


}
