package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        // 返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try (
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // GET请求

    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // POST请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应HTML数据

    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", 30);
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "北京大学");
        model.addAttribute("age", 80);
        return "/demo/view";
    }

    // 响应JSON数据(异步请求)
    // Java对象 -> JSON字符串 -> JS对象

    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        return emp;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "李四");
        emp.put("age", 24);
        emp.put("salary", 9000.00);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name", "王五");
        emp.put("age", 25);
        emp.put("salary", 10000.00);
        list.add(emp);

        return list;
    }

    /**
     * 会话管理：主要是cookie和session机制
     * cookie
     * 1.什么是cookie？理解了这个就理解了cookie的本质。
     * 服务器发送到用户浏览器并保存在本地浏览器的一小块数据，
     * 该数据会在下次浏览器访问同一服务器时被携带并发送到服务器上。
     * 2.为什么使用cookie？
     * http是无状态的，即本次会话和下次会话直接没有任何直接的关系，这样就不方便进行用户登录状态查询等操作，使用cookie可以有效解决这个问题。
     * cookie用于告知服务端两个请求是否来自同一浏览器——如保持用户的登录状态。Cookie使基于无状态的HTTP协议记录稳定的状态信息成为了可能。
     * 3.浏览器中cookie的组成？
     * Cookie是一段不超过4KB的小型文本数据，由一个名称（Name）、一个值（Value）和其它几个用于控制Cookie有效期、安全性、使用范围的可选属性组成。
     * 4.如何创建cookie？
     * 服务器收到 HTTP 请求后，服务器可以在响应标头里面添加一个或多个 Set-Cookie 选项。
     * 浏览器收到响应后通常会保存下 Cookie，并将其放在 HTTP Cookie 标头内，向同一服务器发出请求时一起发送。
     * 你可以指定一个过期日期或者时间段之后，不能发送 cookie。你也可以对指定的域和路径设置额外的限制，以限制 cookie 发送的位置。
     * 更多参考：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Cookies#%E5%AE%9A%E4%B9%89_cookie_%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F
     */
    /**
     * 该方法是cookie的一个小实例，用于服务器返回给浏览器的cookie数据的相关设置
     * 在浏览器按F12打开控制台，点击Network，找到set请求，查看Response Headers下的Set-Cookie。
     * @param response
     * @return
     */
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody  //将controller方法返回的对象通过适当的转换器转换为指定的格式后，写入到reponse对象的body区（响应体中），用来返回JSON数据或者XML；使用此注解不会在走视图处理器，而是直接将数据写入到输入流中；如果不加此注解，返回值将作为url的一部分，页面会跳转到这个url。
    public String setCookie(HttpServletResponse response) {
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID()); //注意这里cookie的版本，这里参数是一个键值对。
        // 设置cookie生效的范围
        cookie.setPath("/community/alpha"); //浏览器只有访问服务器的该路径及其子目录，才会携带cookie信息。
        // 设置cookie生存时间
        cookie.setMaxAge(60 * 10); //设置过期时间，单位是秒，这里就是10分钟
        // 发送cookie
        response.addCookie(cookie); //将cookie对象加入到reponse对象中
        return "set cookie success!";
    }

    //本节老师有个小错误（当然老师可能只是演示一下并不是错误），请求标头中的cookie是否含有code，取决于是第几次访问，与该方法无关，如果是第2次访问，则浏览器会自动将保存的cookie发送到服务器。
    //所以如果没有cookie里的code键值对，访问http://localhost:8080/community/alpha/cookie/get会报错。

    /**
     * 服务器获取cookie内容，并作相应处理，这里是输出到控制台上。
     * @param code
     * @return
     */
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) { //@CookieValue，用来获取Cookie中的值。cookie是在request对象中的，常规使用需要从request中取出所有的cookie，拿出其中的键值对，这里直接使用注解将其中一个键值对拿出来，方便快捷。
        System.out.println(code);
        return "get cookie success!";
    }



}
