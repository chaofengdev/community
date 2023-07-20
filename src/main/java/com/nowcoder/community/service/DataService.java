package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将指定的ip计入uv
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));//将当前日期(new Date())格式化为特定的字符串表示形式
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    //统计指定日期范围内的uv
    public long calculateUV(Date start, Date end) {
        // 检查参数是否为空，如果为空则抛出IllegalArgumentException异常
        if(start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整理该日期范围内的key，保存到集合中
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)) {
            // 获取当前日期对应的Redis key，这个key用于存储当天的UV信息
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);// 将key添加到集合中
            calendar.add(Calendar.DATE, 1);// 将日期往后推一天
        }

        //合并这些数据
        // 使用 Redis 的 HyperLogLog 数据结构将多个日期的UV数据合并到一个新的 Redis key 中
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        //返回统计的结果
        // 使用 Redis 的 HyperLogLog 数据结构计算并返回合并后 Redis key 中的基数估计值，即独立访客数量的估计值
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    //将指定用户计入dau
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    //统计指定范围内的dau
    // 这里每天都有一个一维数组记录所有登录的用户，多天就有多个一维数组，对数组求并集，能得到一段时间内登录的用户。
    public long calculateDAU(Date start, Date end) {
        //判断参数
        if(start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        //整理该日期范围内的key，保存到集合中
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 获取进行OR运算的目标Redis key
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                // 使用bitCount命令统计目标key的位数，即活跃用户数量
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                // 使用bitCount命令统计目标key的位数，即活跃用户数量
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
