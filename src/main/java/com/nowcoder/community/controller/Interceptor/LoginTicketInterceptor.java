package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 自定义的拦截器，用来实现具体的拦截后需要的操作逻辑
 *
 * 它主要用于从浏览器发送的cookie中获取凭证，验证凭证的有效性，并根据凭证查询对应的用户信息。
 * 然后，在本次请求中持有用户对象，将用户数据存储到模板引擎的model中，以便在视图中使用。
 * 最后，在请求完成后清除ThreadLocal中保存的数据，以避免数据泄漏或冲突。
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
                // 构建用户认证的结果并存入SecurityContext，以便于Security进行授权。--这里是为了符合Spring Security框架的要求
                // Authentication表示用户身份验证信息的接口，UsernamePasswordAuthenticationToken是其实现类，使用用户名密码验证
                // user：表示经过身份验证的用户对象。
                // user.getPassword()：表示用户的密码。
                // userService.getAuthorities(user.getId())：表示用户的授权信息。
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), userService.getAuthorities(user.getId()));
                // SecurityContextHolder用于在当前线程中保存安全上下文（Security Context），Security Context包含当前用户的身份认证和授权信息。
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
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
        hostHolder.clear();//清理
        SecurityContextHolder.clearContext();//清理
        //HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
