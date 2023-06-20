package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper //spring容器会自动实例化其实现类的对象，并管理该对象
@Deprecated //用于标记已过时或不推荐使用的代码元素，向开发人员发出警告，表明该代码元素在未来的版本中可能会被移除或替代，建议不再使用。
public interface LoginTicketMapper {

    /**
     * 向数据库中插入login_ticket记录
     * 这里留下一个问题，涉及到ORM框架的源码和基本原理：mybatis是如何实现自动实现loginTicket对象中的属性与sql语句的映射的。
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id") //配置自增长主键，且将主键赋值给实体类对应属性，显然keyProperty指定的是实体类的属性而不是数据库里的字段。自增主键 是从1开始的。
    public int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据ticket查找login_ticket记录
     * @param ticket
     * @return
     */
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    public LoginTicket selectByTicket(String ticket);

    /**
     * 更新登录状态，这里不是直接删除某个login_ticket记录，而是将status置为0，表示登录失效。
     * 根据ticket更新login_ticket记录的status
     * @param ticket
     * @param status
     * @return
     */
    @Update({
            "update login_ticket set status = #{status} ",
            "where ticket = #{ticket}"
    })
    public int updateStatus(String ticket, int status);

}
