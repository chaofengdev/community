package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 自定义的拦截器，用来实现具体的拦截后需要的操作逻辑
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //在controller之前，需要根据浏览器发送过来的cookie中的ticket，查出对应的user
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效 状态为有效、超时时间小于当前时间（时间的比较使用Date类里的after()方法）
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户--这里需要实现user对象的流动，也可以使用request传递user对象。疑问：为什么不直接存到session中？是因为分布式环境下容易出现问题吗？
                hostHolder.setUsers(user);
            }
        }

        return true;
        //return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    //在模板引擎之前，需要将user数据存到model中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUsers();
        if(user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);//将user数据存到model中，用于模板引擎
        }
        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    //请求结束后，将ThreadLocal中保存的数据清除
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        //HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
