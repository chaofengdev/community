package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper  //Mybatis特有的注解，用来标识bean
public interface UserMapper {

    //根据id查询用户
    User selectById(int id);

    //根据用户名查询用户
    User selectByName(String username);

    //根据邮箱查询用户
    User selectByEmail(String email);

    //增加一个用户，返回插入数据的行数
    int insertUser(User user);

    //根据id修改用户的状态，返回修改的数据行数
    int updateStatus(int id, int status);

    //根据id更新头像路径，返回修改的数据行数
    int updateHeader(int id, String headerUrl);

    //根据id更新密码，返回修改的数据行数
    int updatePassword(int id, String password);

}
