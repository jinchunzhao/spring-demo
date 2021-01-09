package com.jy.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义RequestMapping注解
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-09 15:08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JyRequestMapping {
    String value() default "";
}
