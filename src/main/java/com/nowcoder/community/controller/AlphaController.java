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
import javax.servlet.http.HttpSession;
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
     *
     * 2.为什么使用cookie？
     * http是无状态的，即本次会话和下次会话直接没有任何直接的关系，这样就不方便进行用户登录状态查询等操作，使用cookie可以有效解决这个问题。
     * cookie用于告知服务端两个请求是否来自同一浏览器——如保持用户的登录状态。Cookie使基于无状态的HTTP协议记录稳定的状态信息成为了可能。
     *
     * 3.浏览器中cookie的组成？
     * Cookie是一段不超过4KB的小型文本数据，由一个名称（Name）、一个值（Value）和其它几个用于控制Cookie有效期、安全性、使用范围的可选属性组成。
     *
     * 4.如何创建cookie？
     * 服务器收到 HTTP 请求后，服务器可以在响应标头里面添加一个或多个 Set-Cookie 选项。
     * 浏览器收到响应后通常会保存下 Cookie，并将其放在 HTTP Cookie 标头内，向同一服务器发出请求时一起发送。
     * 你可以指定一个过期日期或者时间段之后，不能发送 cookie。你也可以对指定的域和路径设置额外的限制，以限制 cookie 发送的位置。
     * 更多参考：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Cookies#%E5%AE%9A%E4%B9%89_cookie_%E7%9A%84%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F
     *
     * session
     * 1.什么是session？
     * Session代表着服务器和客户端一次会话的过程。Session对象存储特定用户会话所需的属性及配置信息。
     * 这样，当用户在应用程序的 Web 页之间跳转时，存储在 Session 对象中的变量将不会丢失，而是在整个用户会话中一直存在下去。
     * 当客户端关闭会话，或者 Session 超时失效时会话结束。
     *
     * 2.cookie与session的关联？--理解，注意这里的session是指的实现机制。
     * 用户第一次请求服务器的时候，服务器根据用户提交的相关信息，创建创建对应的 Session ，请求返回时将此 Session 的唯一标识信息 SessionID 返回给浏览器，浏览器接收到服务器返回的 SessionID 信息后，会将此信息存入到 Cookie 中，同时 Cookie 记录此 SessionID 属于哪个域名。
     * 当用户第二次访问服务器的时候，请求会自动判断此域名下是否存在 Cookie 信息，如果存在自动将 Cookie 信息也发送给服务端，服务端会从 Cookie 中获取 SessionID，再根据 SessionID 查找对应的 Session 信息，如果没有找到说明用户没有登录或者登录失效，如果找到 Session 证明用户已经登录可执行后面操作。
     * 根据以上流程可知，SessionID 是连接 Cookie 和 Session 的一道桥梁，大部分系统也是根据此原理来验证用户登录状态。
     * 更多参考：https://www.cnblogs.com/ityouknow/p/10856177.html
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


    /**
     * 分布式session共享方案：（分布式情况下，存在session共享的问题）
     * 1、粘性session：在nginx中提供一致性哈希策略，可以保持用户ip进行hash值计算固定分配到某台服务器上，负载也比较均衡，其问题是假如有一台服务器挂了，session也丢失了。
     * 2、同步session：当某一台服务器存了session后，同步到其他服务器中，其问题是同步session到其他服务器会对服务器性能产生影响，服务器之间耦合性较强。
     * 3、共享session：单独搞一台服务器用来存session，其他服务器都向这台服务器获取session，其问题是这台服务器挂了，session就全部丢失。
     * 4、redis集中管理session(主流方法)：redis为内存数据库，读写效率高，并可在集群环境下做高可用。将Session数据传输到redis数据库中，用于保存一些登陆凭证。
     */
    //session示例1:访问http://localhost:8080/community/alpha/session/set，在Response Headers有JSESSIONID字段。
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) { //Spring自动注入该对象
        session.setAttribute("id",1);
        session.setAttribute("name","chaofeng");
        return "set session success!";
    }


    //session示例2:访问http://localhost:8080/community/alpha/session/get，在Request Headers有JSESSIONID字段。
    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session success!" + session.getAttribute("name");//这里有时浏览器显示`get session success!null`，是因为session会话失效（超时？），所以session中的属性不存在，需要重新设置。
    }

    //ajax实例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody //异步请求，向浏览器发送json字符串
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "okk");
    }
}
