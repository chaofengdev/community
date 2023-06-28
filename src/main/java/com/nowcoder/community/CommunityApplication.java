package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct // 常用注解，对象被创建并初始化后，执行该方法。即CommunityApplication被spring容器创建并完成依赖注入后，该方法会被自动调用，用于执行初始化逻辑。
	public void init() {
		// 解决netty启动冲突问题 redis和elasticsearch都用到了Netty作为底层网络通信框架。
		// 详见Netty4Utils.setAvailableProcessors()方法。
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}



	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
