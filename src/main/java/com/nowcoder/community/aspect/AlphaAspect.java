package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * ===============================================================================================================
 * Aspect注解：
 * Aspect 是一个注解，用于声明一个类为切面（Aspect）。切面是在面向切面编程（AOP）中的一个重要概念，用于描述横切关注点的行为。
 *
 * 在使用 Spring Framework 进行 AOP 编程时，您可以使用 @Aspect 注解来定义一个切面类。
 * 切面类通常包含了切点（Pointcut）和通知（Advice）两个主要部分：
 *
 * 切点（Pointcut）：切点用于定义在应用程序中哪些连接点（Join Point）应该被拦截和处理。
 * 连接点是应用程序中可能被切面拦截的特定执行点，如方法调用、方法执行、异常抛出等。通过使用不同的表达式语法，您可以定义出切点来匹配符合特定条件的连接点。
 *
 * 通知（Advice）：通知定义了在切点处执行的逻辑，即切面在特定连接点上执行的行为。
 * 通知可以在连接点之前、之后、周围等不同的时机进行执行，例如在方法调用之前执行一段逻辑，或在方法抛出异常时执行一段处理代码。
 *
 * 通过将 @Aspect 注解应用于类上，您可以告诉 Spring 框架该类是一个切面，它将被用于拦截和处理指定的连接点。
 * 切面类通常还需要与其他 AOP 相关的注解配合使用，例如 @Pointcut 定义切点表达式，@Before、@After、@Around 等定义不同类型的通知。
 *
 *
 * ===============================================================================================================
 *
 */
//@Component
//@Aspect
public class AlphaAspect {

    //切点Pointcut
    @Pointcut("execution(* com.nowcoder.community.controller.HomeController.*(..))") //定义切点：该包下所有类的所有方法都需要处理
    public void pointcut() {
    }

    /**
     * Before 注解用于在目标方法执行之前执行切面逻辑。
     * 常见用例包括参数验证、日志记录、权限检查等。
     */
    @Before("pointcut()")
    public void before() {
        // 在目标方法执行之前执行的操作
        System.out.println("before");
    }

    /**
     * After 注解用于在目标方法执行之后执行切面逻辑。
     * 常见用例包括资源释放、日志记录、性能统计等。
     */
    @After("pointcut()")
    public void after() {
        // 在目标方法执行之后执行的操作
        System.out.println("after");
    }

    /**
     * AfterReturning 注解用于在目标方法成功返回结果后执行切面逻辑。
     * 通常用于日志记录、缓存处理、事务管理等。
     */
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    /**
     * AfterThrowing 注解用于在目标方法抛出异常后执行切面逻辑。
     * 通常用于异常处理、日志记录、异常转换等。
     */
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    /**
     * Around 注解用于在目标方法执行前后控制切面逻辑的执行。
     * 通常用于拦截目标方法的调用、修改方法的返回值或参数、处理异常等。
     * @param joinPoint
     * @return
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 在目标方法执行前的逻辑
        System.out.println("around:before");
        Object obj = joinPoint.proceed();// 执行目标方法
        // 在目标方法执行后的逻辑
        System.out.println("around:after");
        return obj;
    }
}
