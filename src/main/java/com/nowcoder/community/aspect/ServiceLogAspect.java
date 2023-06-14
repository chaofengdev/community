package com.nowcoder.community.aspect;

import com.nowcoder.community.controller.advice.ExceptionAdvice;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //切点Pointcut
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))") //定义切点：该包下所有类的所有方法都需要处理
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {//通过 joinPoint 参数，您可以获取与连接点相关的信息，例如被拦截的方法的签名、方法的参数等。
        //日志格式：用户[167.221.32.44]，在[xxx]，访问了[com.nowcoder.community.service.xxx()].
        //获取request  使用了 Spring 框架提供的 RequestContextHolder 和 ServletRequestAttributes 类来获取当前请求的 HttpServletRequest 对象。
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //根据request可以方便地获取到当前请求的相关信息，如请求的 URL、请求的参数、请求的头信息等。
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();//类型名、方法名
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip,now,target));
    }
}
