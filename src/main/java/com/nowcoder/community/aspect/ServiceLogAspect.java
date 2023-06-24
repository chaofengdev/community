package com.nowcoder.community.aspect;

import com.nowcoder.community.controller.advice.ExceptionAdvice;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * RequestContextHolder.getRequestAttributes();
 * RequestContextHolder 是 Spring 框架提供的一个用于访问当前请求上下文的工具类。
 * getRequestAttributes() 是 RequestContextHolder 类中的静态方法，它返回当前线程的请求属性对象，该对象存储了与当前请求相关的信息。
 * 请求属性对象包含了请求的各种属性，例如请求参数、会话信息、请求头等。
 * 使用 RequestContextHolder.getRequestAttributes() 可以在任何地方获取当前请求的属性对象，而无需传递 HttpServletRequest 对象作为参数。
 * 返回的请求属性对象是一个 ServletRequestAttributes 类型的实例，它是 ServletRequestAttributes 类的子类，提供了更具体的方法来访问请求属性。
 */
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
        if(attributes == null) {//防止空指针异常 增加消费者后，消费者会调用Service层方法，此时attributes为空。--没有完全理解。
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //根据request可以方便地获取到当前请求的相关信息，如请求的 URL、请求的参数、请求的头信息等。
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();//类型名、方法名
        logger.info(String.format("用户[%s],在[%s],访问了[%s]",ip,now,target));
    }
}
