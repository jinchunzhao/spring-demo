package com.jy.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义controller注解
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-09 12:11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JyController {
    String value() default "";
}
