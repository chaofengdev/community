package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 继承WebSecurityConfigurerAdapter类可以方便地自定义和配置Spring Security的行为。通过覆盖该类中的方法，可以进行以下操作：
 *
 * 配置身份验证（Authentication）：
 * 通过覆盖configure(AuthenticationManagerBuilder auth)方法，可以配置用户认证的方式。
 * 可以指定内存中的用户、数据库中的用户、LDAP等作为认证的来源，并配置密码加密、自定义认证逻辑等。
 *
 * 配置授权（Authorization）：
 * 通过覆盖configure(HttpSecurity http)方法，可以配置URL的访问权限，指定哪些URL需要哪些角色或权限才能访问。
 * 可以配置基于角色的访问控制、表达式授权、自定义访问决策等。
 *
 * 配置Web安全性：
 * 通过覆盖configure(WebSecurity web)方法，可以配置Spring Security对静态资源的忽略、设置全局安全策略、配置Session管理等。
 *
 * 配置身份验证过滤器：
 * 通过覆盖configure(HttpSecurity http)方法，可以添加自定义的身份验证过滤器，对请求进行身份验证的处理。
 *
 * 使用WebSecurityConfigurerAdapter可以更加灵活地配置和定制Spring Security，适应不同的安全需求和场景。
 * 它提供了一种便捷的方式来扩展和自定义Spring Security的行为，使开发者能够轻松地集成认证和授权功能到他们的Web应用程序中。
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    //忽略静态资源
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 配置请求的授权规则
        http.authorizeRequests()
                .antMatchers( //这些路径需要登录之后才能访问
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority( //登录需要的权限
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .anyRequest().permitAll()//其他任何请求都通过
                .and().csrf().disable();//禁用 CSRF（Cross-Site Request Forgery）攻击检查

        // 配置异常处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {//没有登录时的处理
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        // 请求头部字段X-Requested-With，表明请求是通过XHR（XMLHttpRequest）发送的，常用于AJAX请求
                        String header_field = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(header_field)) {//异步请求，发送json字符串
                            // 响应头部字段Content-Type，表明服务器返回的响应正文（Response Body）的媒体类型。Content-Type字段的值是MIME类型，常见的包括：
                            // text/html：HTML文档 text/plain：纯文本 application/json：JSON数据 application/xml：XML数据 image/jpeg：JPEG图像 application/pdf：PDF文档
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            // 获取PrintWriter对象，用于向客户端发送响应数据
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦！"));
                        } else {//非异步请求，重定向到指定页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {//权限不足时的处理
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String header_field = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(header_field)) {//异步请求，发送json字符串
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有访问此功能的权限！"));
                        } else {//非异步请求，重定向到指定页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行自己的退出代码，这里指定一个不会访问的注销路径，这样我们的/logout逻辑才会执行。
        http.logout().logoutUrl("securitylogout");//当用户访问指定的注销URL路径时，Spring Security 将执行默认的注销操作，包括清除用户的身份验证凭据并使其注销。
    }
}
