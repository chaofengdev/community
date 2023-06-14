package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解只起到标记的作用。
 * 被该注解注释的方法，只有登录后才能访问。
 * 实际逻辑是通过拦截器实现的。
 */
@Target(ElementType.METHOD) //声明注解在方法上有效
@Retention(RetentionPolicy.RUNTIME) //声明注解的保留时间
public @interface LoginRequired {
}
