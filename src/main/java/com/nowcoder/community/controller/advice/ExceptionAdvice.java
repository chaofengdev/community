package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * -----------------------------------------------------------------------------------------------------------------
 * ** ControllerAdvice注解
 * ControllerAdvice注解是Spring框架中的一个注解，用于定义全局的异常处理和全局数据绑定操作。
 * 通过将@ControllerAdvice注解应用到类上，可以将该类标识为全局的异常处理类或全局数据绑定类，以便在应用中统一处理异常或执行全局数据绑定操作。
 *
 * 具体而言，@ControllerAdvice注解通常与其他注解一起使用，
 * 比如@ExceptionHandler用于定义异常处理方法，@InitBinder用于定义数据绑定方法，@ModelAttribute用于定义全局模型属性等。
 * 这样，通过将这些注解应用到@ControllerAdvice注解标识的类中的方法上，可以实现对全局异常的捕获和处理，以及对全局数据的绑定和预处理。
 *
 * 总之，@ControllerAdvice注解提供了一种简洁的方式来定义全局的异常处理和全局数据绑定操作，使得开发者可以在一个地方集中管理和处理多个控制器中的异常和数据绑定需求。
 * -----------------------------------------------------------------------------------------------------------------
 * ** 垂直边距线
 * 另外：IntelliJ idea中，编辑器中有个竖线，叫做垂直边距线。
 * 垂直边距线（Vertical Ruler）是 IntelliJ IDEA 编辑器中的一条竖线，用于提供辅助对齐和参考。
 * 它位于编辑器的左侧或右侧，可以帮助您在编写代码时进行对齐操作或标记重要位置。
 * 在传统的代码风格中，80列边距线用于指示代码应该保持的最大行宽。这有助于编写遵循特定代码风格规范的代码。
 * 80列边距线的宽度不是一个固定值，本机环境实测是120个字符。
 * ------------------------------------------------------------------------------------------------------------------
 * 关于Advice与AOP的关系：
 * Advice属于Spring MVC的内容，使用它便于获取与请求有关的参数，如Request、Response，这样就很方便做重定向。
 * 所以，能使用Advice、Interceptor解决的，我们就用。不能使用他们解决的，我们才会选择AOP。
 * 同样，拦截器也是AOP思想的一种实现，Spring AOP也是AOP思想的一种实现。
 * ------------------------------------------------------------------------------------------------------------------
 */
@ControllerAdvice(annotations = Controller.class) //表明对所有@Controller注解的类生效
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})//表示对Exception异常生效，即对所有异常生效
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());//将拼接后的错误消息作为日志信息记录下来，具体记录方式在配置文件logback.xml中
        for(StackTraceElement element : e.getStackTrace()) {//打印异常堆栈轨迹信息到日志中
            logger.error(element.toString());
        }
        //根据异步请求还是正常请求区分返回数据
        //对于异步请求，返回一个包含错误信息的 JSON 字符串；对于正常请求，将请求重定向到错误处理页面或错误提示页面，以展示更友好的错误信息给用户。
        String xRequestedWith = request.getHeader("x-requested-with");//获取请求头中的 "x-requested-with" 字段的值，该字段用于标识请求是异步请求还是正常请求。
        if("XMLHttpRequest".equals(xRequestedWith)) {//"XMLHttpRequest" 是一个常见的 HTTP 请求头字段，用于标识请求是通过 XMLHttpRequest 对象发起的异步请求。XMLHttpRequest 是一种用于在后台与服务器进行数据交互的技术，常用于实现 AJAX（Asynchronous JavaScript and XML）方式的异步通信。
            response.setContentType("application/plain;charset=utf-8");//设置响应的内容类型为 "application/plain;charset=utf-8"，表示响应内容为纯文本
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));//将自定义的错误信息写入响应中
        } else {
            response.sendRedirect(request.getContextPath() + "/error");//将请求重定向到 "/error" 路径。这个路径对应的方法是getErrorPage()。
        }
    }

}
