package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截器：拦截未登录情况下对特定方法的请求。
 * 主要是为了防止用户在未登录的情况下，通过特定url访问特定的页面，保证项目的安全。
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {//HandlerMethod封装了很多属性，在访问请求方法的时候可以方便的访问到方法、方法参数、方法上的注解、所属类等并且对方法参数封装处理，也可以方便的访问到方法参数的注解等信息。
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);//获取方法上的特定注解
            if(loginRequired != null && hostHolder.getUsers() == null) {//有登录注解但没有登录用户
                //在程序中，可以通过以下方式正确实现重定向：
                //在Controller的方法里，通过返回以“redirect”开头的字符串实现重定向。
                //在Controller的方法里，通过response对象的sendRedirect方法实现重定向。
                //在拦截器中，通过response对象的sendRedirect方法实现重定向。
                //在拦截器中，通过返回以“redirect”开头的字符串实现重定向是不正确的。拦截器无法通过返回字符串来实现重定向，而应使用response对象的sendRedirect方法。
                response.sendRedirect(request.getContextPath() + "/login");//强制重定向到登录页面 request.getContextPath()返回当前页面所在的应用的名字
                return false;
            }
        }
        return true;//通行。
        //return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
