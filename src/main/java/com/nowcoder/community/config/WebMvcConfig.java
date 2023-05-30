package com.nowcoder.community.config;

import com.nowcoder.community.controller.Interceptor.AlphaInterceptor;
import com.nowcoder.community.controller.Interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类
 * WebMvcConfigurer是Spring内部的配置方式，用来针对框架进行个性化定制，可以自定义一些Handler、Interceptor、ViewResolver、MessageConverter，
 * 基于java-based方式的spring mvc配置，需要创建一个配置类并实现WebMvcConfigurer接口。
 * 官方推荐直接实现WebMvcConfigurer（推荐做法）或者直接继承WebMvcConfigurationSupport。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {//WebMvcConfigurer接口中含有大量的default方法，该方法为接口抽象方法的默认实现。

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置拦截器的具体属性：排除部分对象，需要拦截的访问路径
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");

        //除了静态资源外，拦截所有请求。
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        //WebMvcConfigurer.super.addInterceptors(registry);
    }
}
