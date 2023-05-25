package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration //该注解用于定义配置类，可替换xml文件；被注解的类内部包含多个被@Bean注解的方法，这些方法的返回对象将被spring容器管理，并可以在其他类中使用@Autowired注入。
public class KaptchaConfig {
    @Bean //方法的返回值将被spring容器管理和装配
    public Producer KaptchaProducer() {//Producer接口是kaptcha核心接口，默认实现类是DefaultKaptcha，跟踪源码得知，其实现使用了java.awt.xxx包。
        //属性文件中的配置值，也可以在对应xxx.properties文件中通过键值对指定，这里直接写在代码中
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");//这里的属性都来自官网，没有字符串检查，所以务必写对
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");//验证码范围
        properties.setProperty("kaptcha.textproducer.char.length", "4");//验证码长度
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");//默认噪声，即图片的干扰程度

        //使用对应api生成DefaultKaptcha实例化对象，并交给spring管理，方便在其他地方注入
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);//Config从属性文件中检索配置值，并在未指定值时指定默认值。
        kaptcha.setConfig(config);//DefaultKaptcha继承抽象类Configurable，setConfig定义在Configurable内。
        return kaptcha;
    }
}
