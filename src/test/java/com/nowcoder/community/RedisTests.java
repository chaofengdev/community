package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        /**
         * redisTemplate：这是一个RedisTemplate实例，用于与Redis进行交互。
         * opsForValue()：这是RedisTemplate的方法之一，返回一个用于操作Redis字符串值的ValueOperations对象。
         * set(redisKey, 1)：这是ValueOperations对象的set()方法，用于将指定值存储到指定的键中。在这里，将值1存储到键为redisKey的位置。
         */
        redisTemplate.opsForValue().set(redisKey,1);//设值
        System.out.println(redisTemplate.opsForValue().get(redisKey));//取值
        System.out.println(redisTemplate.opsForValue().increment(redisKey));//值加1
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));//值减1
    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";//这里可以将redisKey理解为哈希表的名称？
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists() {
        String redisKey = "test:ids";//这里可以将redisKey理解为无序表的名称？
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "chaofeng", "jiangjie", "wenhao", "lihua");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));//随机弹出
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "chaofeng", 100);
        redisTemplate.opsForZSet().add(redisKey, "lihua", 88);
        redisTemplate.opsForZSet().add(redisKey, "boyue", 80);
        redisTemplate.opsForZSet().add(redisKey, "wenhao", 97);
        redisTemplate.opsForZSet().add(redisKey, "jiangjie", 79);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));//统计多少个数据
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"chaofeng"));//某个人分数
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"chaofeng"));//分数排名（从0开始），从小到大 reverseRank()从大到小
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,3));//分数排名，从小到大 reverseRank()从大到小
    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);//需要指定时间单位
        System.out.println(redisTemplate.hasKey("test:students"));
    }

    //多次访问同一key
    //将redisKey与redisTemplate绑定，可以直接使用BoundValueOperations类型对象操作redisKey数据
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());//提前绑定，不需要传入redisKey
    }

    //编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {//匿名内部类
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();//Redis事务的开始操作，用于标记事务的开始位置。
                operations.opsForSet().add(redisKey, "chaofeng");
                operations.opsForSet().add(redisKey, "jiangjie");
                operations.opsForSet().add(redisKey, "lihua");
                operations.opsForSet().add(redisKey, "boyue");
                System.out.println(operations.opsForSet().members(redisKey));//[]
                return operations.exec();//提交事务 事务中的命令并不会立即执行，而是被添加到事务队列中。只有在调用exec()时，才会将整个事务一起执行。
            }
        });
        System.out.println(obj);//[0, 0, 0, 0, [boyue, jiangjie, lihua, chaofeng]]
    }
}
