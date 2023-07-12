package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
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

    //统计20w个重复数据的独立总数
    // HyperLogLog是一种基数估计算法，用于统计集合中不重复元素的数量。它可以在很小的内存占用下，对非常大的数据集进行基数估计。
    // HyperLogLog 在很多场景下都有广泛的应用，例如网站访问量统计、社交网络分析、广告点击统计等。
    // UV" 通常指的是"独立访客"（Unique Visitors）。"独立访客" 是指在特定时间范围内访问网站的唯一个体或设备数量。
    // 在数据处理中，基数（cardinality）是指集合中不重复元素的数量。HyperLogLog 可以在处理大规模数据时快速而高效地估计集合的基数。
    @Test
    public void testHyperLogLog() {
        //创建了一个键为 "test:hll:01" 的 HyperLogLog 结构
        String redisKey = "test:hll:01";

        //模拟添加了 100,000 个不重复的数据
        for (int i = 0; i < 100000; i++) {
            //HyperLogLog是一种基数估计算法，用于计算集合中不重复元素的数量。
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        //模拟添加了 100,000 个随机重复的数据
        for (int i = 0; i < 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);//随机数范围[1,100000]
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        //获取 HyperLogLog 结构的估计基数，即独立总数
        long redisKey_size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println("独立总数：" + redisKey_size);
    }

    //将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redis_key2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redis_key2, i);
        }

        String redis_key3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redis_key3, i);
        }

        String redis_key4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redis_key4, i);
        }

        // union用于计算多个 HyperLogLog 结构的并集，并将结果存储在一个新的 HyperLogLog 结构中
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redis_key2, redis_key3, redis_key4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    //统计一组数据的布尔值
    @Test
    public void testBitMap() {
        // 定义了一个 Redis 键 redisKey，用于存储位图数据。
        String redisKey = "test:bm:01";

        //记录 将指定位置的位值设置为 true（1）。在示例中，将第 1、4 和 7 位置的位值设置为 true。
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        //查询 查询指定位置的位值。在示例中，输出了第 0、1 和 2 位置的位值，即第 0 位为 false（0），第 1 位为 true（1），第 2 位因为没有设置过，所以返回默认值 false（0）。
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        //统计 统计位图中被设置为 true 的位数，并将结果返回。
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //bitCount命令用于计算位图中被设置为 1 的位的数量。
                return connection.bitCount(redisKey.getBytes());//将 Redis 键 redisKey 转换为字节数组形式
                //return null;
            }
        });
        System.out.println(obj);
    }

    //统计3组数据的布尔值，并对这3组数据做OR运算
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);
        redisTemplate.opsForValue().setBit(redisKey2, 5, true);
        redisTemplate.opsForValue().setBit(redisKey2, 6, true);

        //合并的结果，保存在新键redisKey
        String redisKey = "test:dm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),
                        redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }
}
