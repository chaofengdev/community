package com.nowcoder.community.config;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.controller.Interceptor.AlphaInterceptor;
import com.nowcoder.community.controller.Interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.Interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.Interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置类
 * WebMvcConfigurer 是 Spring MVC 提供的一个接口，用于配置和定制化 Web MVC（Model-View-Controller）相关的行为。
 * 通过实现该接口并重写其中的方法，可以对 Spring MVC 进行各种配置和扩展。
 * 基于java-based方式的spring mvc配置，需要创建一个配置类并实现WebMvcConfigurer接口。
 * 官方推荐直接实现WebMvcConfigurer（推荐做法）或者直接继承WebMvcConfigurationSupport。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {//WebMvcConfigurer接口中含有大量的default方法，该方法为接口抽象方法的默认实现。

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    /**
     * 下面定义的拦截器的实际执行的先后顺序：由注册的顺序决定。
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置拦截器的具体属性：排除部分对象，需要拦截的访问路径
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");

        //除了静态资源外，拦截所有请求。
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        //除了静态资源外，拦截所有请求。
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        //WebMvcConfigurer.super.addInterceptors(registry);

        //除了静态资源外，拦截所有请求。
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
