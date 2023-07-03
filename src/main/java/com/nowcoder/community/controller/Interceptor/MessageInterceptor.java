package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义的拦截器，实现HandlerInterceptor接口
 *
 * 当请求处理完成后，letterUnreadCount和allUnreadCount 属性将被添加到 modelAndView 对象中，
 * 在视图中使用${letterUnreadCount} ${allUnreadCount} 表达式来获取该值进行展示。
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    /**
     * 在请求处理完成后，对 ModelAndView 进行后处理操作。
     * 如果存在用户且 modelAndView 不为 null，则调用 messageService 的方法获取未读私信数量和未读通知数量，
     * 并将它们相加后添加到 modelAndView 中的 allUnreadCount 属性上。
     * 当请求处理完成后，allUnreadCount 属性将被添加到 modelAndView 对象中，
     * 并可以在视图中使用 ${allUnreadCount} 表达式来获取该值进行展示。
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        User user = hostHolder.getUsers();
        if(user != null && modelAndView != null) {
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
