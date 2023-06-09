package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.util.CommunityConstant.*;


@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptcha;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;


    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);//注意类型匹配


    /**
     * 来自菜鸟教程
     * Form 中的 get 和 post 方法，在数据传输过程中分别对应了 HTTP 协议中的 GET 和 POST 方法。二者主要区别如下：
     *  1、Get 是用来从服务器上获得数据，而 Post 是用来向服务器上传递数据。
     *  2、Get 将表单中数据的按照 variable=value 的形式，添加到 action 所指向的 URL 后面，并且两者使用“?”连接，而各个变量之间使用“&”连接；Post 是将表单中的数据放在 form 的数据体中，按照变量和值相对应的方式，传递到 action 所指向 URL。
     *  3、Get 是不安全的，因为在传输过程，数据被放在请求的 URL 中，而如今现有的很多服务器、代理服务器或者用户代理都会将请求URL记录到日志文件中，然后放在某个地方，这样就可能会有一些隐私的信息被第三方看到。另外，用户也可以在浏览器上直接看到提交的数据，一些系统内部消息将会一同显示在用户面前。Post 的所有操作对用户来说都是不可见的。
     *  4、Get 传输的数据量小，这主要是因为受 URL 长度限制；而 Post 可以传输大量的数据，所以在上传文件只能使用 Post（当然还有一个原因，将在后面的提到）。
     *  5、Get 限制 Form 表单的数据集的值必须为 ASCII 字符；而 Post 支持整个 ISO10646 字符集。
     *  6、Get 是 Form 的默认方法。
     * 使用 Post 传输的数据，可以通过设置编码的方式正确转化中文；而 Get 传输的数据却没有变化。在以后的程序中，我们一定要注意这一点。
     * @return
     */
    /**
     * Spring MVC 的 @RequestMapping 注解能够处理 HTTP 请求的方法, 比如GET, PUT, POST, DELETE 以及 PATCH。
     * 所有的请求默认都会是 HTTP GET 类型的。
     * 为了能降一个请求映射到一个特定的HTTP方法，你需要在 @RequestMapping 中使用method来声明HTTP请求所使用的方法类型。
     * @return
     */
    //访问该路径，返回注册视图
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    //访问该路径，返回登录视图
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {//注意：这里springboot会将user对象自动注入到model中，思考一下注入的时机？可以显式手动注入吗？
        //调用register获取map
        Map<String, Object> map = userService.register(user);

        //map为空表示注册成功并返回中间结果视图（会自动跳转到首页），不为空表示注册失败返回相关消息并返回到注册视图
        if(map == null || map.isEmpty()) { //疑问：这里map一定会被实例化，为什么要判断map==null
            model.addAttribute("msg", "注册成功，我们已向您邮箱发送了一条用于激活的邮件，请尽快激活！");
            model.addAttribute("target", "/index");//中间结果视图会跳转到首页，这是中间结果视图需要的model信息
            return "/site/operate-result";
        }else {
            //没有注册成功，本质上是service层判断不能通过，将service层的错误数据提取出来封装到model中，返回给注册页面，注册页面再进行相关展示
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8080/community/activation/101/code 前端访问路径
    @RequestMapping(path = "/actionvation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable(name = "userId") int userId, @PathVariable(name = "code") String code) { //将url中占位符参数{xxx}绑定到处理器类的方法形参中，这里只是恰好占位符参数名称与处理器类方法形参相同
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");//给operate-result视图使用，方便点击链接，跳转到登录页面
        }else if(result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");//跳转到首页
        }else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");//跳转到首页
        }
        return "/site/operate-result";//中间结果页面
    }

    /**
     * 生成验证码并将验证码输出给浏览器
     * @param response
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){//这里返回给页面的是图片，不是页面或者字符串
        // 生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);//BufferedImage来自java.awt.image

        // 将验证码存入session （出于性能考虑，已重构，将验证码text存放到redis中）
        // session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();//生成一个标识符（随机字符串），这个标识符每次点击刷新验证码都会改变。
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入Redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);//Set the value and expiration timeout for key.

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();//字符流
            ImageIO.write(image, "png", os);//说实话不太熟悉，先用吧。
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     * 验证登录是否成功，如果成功重定向到首页，如果不成功回到登录页面。
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param response
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme, Model model/*, HttpSession session*/, HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");//session中取出验证码

        //检查验证码（重构）
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {//判断验证码是否正确
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";//验证码不正确重新回到登录页面
        }
        //检查账号、密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;//根据是否勾选保存，得到凭证的保存时间
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //为什么要将ticket加到cookie中并发送到客户端？
        //为了在客户端与服务器之间建立会话并实现用户身份验证和状态管理。
        //在客户端发送请求时，浏览器会自动将Cookie作为请求头的一部分发送到服务器，服务器就可以读取其中的凭证，并根据凭证判断用户的身份和权限。
        //详见拦截器LoginTicketInterceptor，preHandle里每次根据cookie中的ticket查出user，将user存在hostHolder对象中，
        //postHandle里将user数据存到model中，用于模板引擎，请求结束后，将ThreadLocal中保存的数据清除；
        //这样可以实现持久化的会话管理，允许用户在一段时间内保持登录状态，而无需在每个请求中都重新进行身份验证。
        if(map.containsKey("ticket")) {//包含凭证
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());//将凭证保存到cookie发送给客户端
            cookie.setPath(contextPath);//路径
            cookie.setMaxAge(expiredSeconds);//cookie有效时间
            response.addCookie(cookie);//利用reponse，服务器响应浏览器时，将cookie发送给浏览器
            return "redirect:/index";//重定向到首页 思考一下这里为什么不forward而是redirect？简单的解释是直接返回"/index"返回的是视图，返回"redirect:/index"表示重新发起请求。
        }else {//没有凭证，说明发生了错误
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";//重新回到登录页面
        }
    }

    /**
     * 退出功能。
     * 接收到客户端传入的cookie，得到ticket。
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();//清理
        return "redirect:/login";//重定向默认是get请求
    }
}
