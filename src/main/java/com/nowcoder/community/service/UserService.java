package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.util.CommunityConstant.*;

//因为注册、登录的相关功能是对用户表的操作，所以登录的相关业务代码写到UserService内。
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;//发送邮件功能

    @Autowired
    private TemplateEngine templateEngine;//邮件中需要呈现html数据，发送带有html内容的邮件需要这个对象

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;//登录验证功能

    @Value("${community.path.domain}")
    private String domain;//域名，这里指的是邮件内的验证网址，点击该网址跳转到验证页面，注意这里只有ip地址和端口号

    @Value("${server.servlet.context-path}")
    private String contextPath;//项目名称，用于和上面的domain拼接成完整的访问路径

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据id查询用户
     * @param id 用户id
     * @return 查询后返回的用户对象
     */
    public User findUserById(int id) {
        //return userMapper.selectById(id);//重构以提高性能
        //尝试从redis中取user
        User user = getCache(id);
        //根据user是否存在
        if(user == null) {//user不在redis中
            user = initCache(id);//从mysql中查询user并存到redis中
        }
        return user;
    }

    /**
     * 注册用户的业务逻辑，传入用户对象，返回结果集合，核心逻辑是先判断传入的对象是否合法，如果合法就补充对象相关信息，并插入数据库
     * @param user 用户对象
     * @return map集合，保存错误消息的键值对，为空表示正常注册
     */
    public Map<String, Object> register(User user) {
        //保存错误消息类型和详细错误消息对象的键值对
        Map<String, Object> map = new HashMap<>();

        //空值的处理
        if(user == null) {//用户为空，直接抛出异常，无法处理
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        //验证账号是否已经存在
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg","该账号已存在！");//注意这里usernameMsg的value值已经更改
            return map;
        }

        //验证邮箱是否已经被注册
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg","该邮箱已被注册！");//注意这里emailMsg的value值已经更改
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));//调用工具类生成salt
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));//调用工具类生成密码
        user.setType(0);//类型 0-普通用户; 1-超级管理员; 2-版主;
        user.setStatus(0);//状态 0-未激活; 1-已激活; 初始未激活
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));//头像地址  String类的format()方法用于创建格式化的字符串以及连接多个字符串对象。
        user.setCreateTime(new Date());//注册时间
        //将注册用户添加到数据库中
        userMapper.insertUser(user);//插入对象，数据库主键自增，mybatis会自动获取自增的主键并填充回user对象中，此时user中有id。--配置文件中的相关设置：mybatis.configuration.useGeneratedKeys=true

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //拼接正确的激活地址：http://localhost:8080/community/activation/101/code
        //解析：http://ip地址:端口/项目名/功能名/用户id/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println(url);
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        // 这里复习一下try-catch的使用逻辑：
        // 当没有try-catch时，出现异常程序直接崩溃并将一大堆崩溃信息展示给用户，用户可能会勃然大怒觉得写的是什么东西；
        // 加上try-catch后程序正常运行，只是将错误信息存储到Exception里，供程序员提取部分信息展示给用户。
        // 比如前端表单填写的是错误的目的邮箱，这里会报错com.sun.mail.smtp.SMTPSendFailedException: 550 The recipient may contain a non-existent account, please check the recipient address.
        try {
            mailClient.sendMail(user.getEmail(), "激活账号", content);
        }catch (Exception e) {
            e.printStackTrace();//现阶段的代码，如果输入的邮箱不存在会出异常，应该在前端展示邮箱不存在。这里简单处理一下。
        }

        //返回map集合
        return map;
    }

    /**
     * 验证激活是否成功。
     * 核心逻辑是将激活链接的激活码与数据库中的用户激活字段比较，如果相同，则修改用户状态字段并返回激活成功常量；
     * 同时需要判断用户是否已经激活，避免重复激活。
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);

        if(user.getStatus() == 1) {//用户已经激活
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);//激活用户
            clearCache(userId);//清理redis缓存中的user
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;//不满足上述条件表示激活失败
        }
    }

    /**
     * 根据username，password和expiredSeconds，判断某个用户是否登录成功
     * @param username
     * @param password
     * @param expiredSeconds 单位秒
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 验证状态
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        // 验证密码--根据待登录用户传入的密码，进行md5加密，与数据库中保存的加密密码对比
        String md5_password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(md5_password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 生成登录凭证--检查完成，确认用户登录成功，生成登录凭证，并保存到数据库中，用于保持登录状态
        // 这里用ticket来代替session，核心逻辑：用户登录完成后，下次再次访问时将ticket发送给服务器，服务器在login_ticket数据库中找到记录，则认为用户在登录状态。
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());//随机生成凭证
        loginTicket.setStatus(0);//0表示有效，1表示无效
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSeconds));
        //loginTicketMapper.insertLoginTicket(loginTicket);//重构
        //将loginTicket对象序列化成字符串后保存到redis数据库中
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);//loginTicket对象序列化后存入redis

        map.put("ticket", loginTicket.getTicket());//Controller使用的数据--凭证
        return map;
    }

    /**
     * 退出功能
     * @param ticket
     */
    public void logout(String ticket) {
        //loginTicketMapper.updateStatus(ticket, 1);//凭证置为无效
        //重构，从ticketKey中取出loginTicket，将loginTicket修改登录状态status后，重新存入ticketKey中
        //chatgpt：访问Redis数据库，找到特定凭证对应的登录凭证对象，并将其状态标记为失效。然后，更新后的登录凭证对象被再次存储回Redis中，以确保失效状态的生效。
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);//1表示用户登录状态失效
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 根据ticket字符串查询凭证
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
        //return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);//注意默认返回的都是Object，所以需要强转。

    }

    /**
     * 更新指定用户的头像链接地址
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);//清理redis缓存
        return rows;
    }

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //1.优先从缓冲中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //2.取不到时，初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);//从mysql查询数据
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);//存放到redis
        return user;
    }

    //3.数据变更时，清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
