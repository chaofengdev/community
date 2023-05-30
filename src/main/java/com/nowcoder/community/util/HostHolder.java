package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 *
 * 1.这里需要详细解释一下：“用于代替session对象”
 * 因为配置了拦截器，所以这里需要有一种共享机制，能够在各层共享user对象，
 * 首先想到的是session，但是session在分布式环境下不能有效的共享，我们使用mysql来代替；--详见分布式session问题
 * 其次想到request，但是request是比较底层的对象，并不是所有位置都方便获取到request；
 * 最后是TheadLocal，可以在一个线程中传递同一个对象，可以实现在本次请求中持有user对象并在各层使用。
 * 2.补充关于ThreadLocal的解释
 * 我们遇到一个问题：如何在一个线程内传递状态？
 * 这种在一个线程中，横跨若干方法调用，需要传递的对象，我们通常称之为上下文（Context），它是一种状态，可以是用户身份、任务信息等。
 * 给每个方法增加一个context参数非常麻烦，而且有些时候，如果调用链有无法修改源码的第三方库，User对象就传不进去了。
 * Java标准库提供了一个特殊的ThreadLocal，它可以在一个线程中传递同一个对象。
 * 3.参考连接：https://www.liaoxuefeng.com/wiki/1252599548343744/1306581251653666
 *
 */
@Component
public class HostHolder {
    //用于在一个线程中传递同一个对象。
    //可以把ThreadLocal看成一个全局Map<Thread, Object>：每个线程获取ThreadLocal变量时，总是使用Thread自身作为key
    //因此，ThreadLocal相当于给每个线程都开辟了一个独立的存储空间，各个线程的ThreadLocal关联的实例互不干扰。
    private ThreadLocal<User> users = new ThreadLocal<>();//这里其实命名为context更好

    public void setUsers(User user) {//在本线程里放入user对象
        users.set(user);
    }

    public User getUsers() {//获取本线程里的user对象
        return users.get();
    }

    public void clear() {//清除本线程里的user对象
        users.remove();
    }
}
