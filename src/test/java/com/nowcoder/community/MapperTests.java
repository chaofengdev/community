package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {
    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("chenchaofeng");
        user.setPassword("123456");
        user.setSalt("123");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/100t.png");
        user.setCreateTime(new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());//数据库自增id字段后，返回给user对象的id属性
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updatePassword(150, "23333");
        System.out.println(rows);

        rows = userMapper.updateHeader(150,"http://images.nowcoder.com/head/101.png");
        System.out.println(rows);

        rows = userMapper.updateStatus(150,1);
        System.out.println(rows);
    }

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post.toString());
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    /**
     * 测试插入login_ticket记录
     * 这里需要简单测试，因为sql语句经常容易写错，并且IDE不会自动纠错。
     */
    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(102);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);//表示未启用
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 *10));//当前时间的后十分钟
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    /**
     * 测试查询和更新login_ticket记录
     */
    @Test
    public void testSelectLoginTicket() {
        //查询
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        //更新
        int rows = loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");//验证更新的查询
        System.out.println(loginTicket);
    }
}
