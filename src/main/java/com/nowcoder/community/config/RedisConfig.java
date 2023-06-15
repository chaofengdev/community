package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean//Spring容器在创建这个Bean的时候，看到它依赖其他的Bean就会自动注入，这个参数实际上就是依赖的体现。
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {//spring自动注入RedisConnectionFactory
        //RedisConnectionFactory负责建立和管理与Redis服务器的连接。
        //它提供了必要的信息，如主机名、端口和身份验证等，以连接到Redis服务器。
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);//盲猜是将相关信息通过set传给template对象。等有空看看源码。

        /*
        * Redis是一个键值存储数据库，它只能存储字符串格式的数据，而不能直接存储Java对象。
        * 通过设置序列化方式，您可以控制将Java对象序列化为字符串存储在Redis中的方式，
        * 以及在从Redis中检索数据时将存储的字符串反序列化为Java对象的方式。
        * 这样可以确保在与Redis交互时，数据的格式正确并且可以正确地进行存储和读取。
         * */
        //设置key序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        //返回值被注入到spring容器中
        return template;
    }
}
