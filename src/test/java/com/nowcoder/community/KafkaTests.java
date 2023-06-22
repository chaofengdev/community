package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class) //指定测试运行器为Spring Runner，以便在测试中使用Spring的功能。
@SpringBootTest //Spring Boot的集成测试，将启动完整的Spring应用程序上下文。
@ContextConfiguration(classes = CommunityApplication.class) //指定要加载的Spring应用程序上下文的配置类。
public class KafkaTests {

    @Autowired
    private KafkaProducer kafkaProducer;

    /**
     * 测试类中的testKafka方法演示了发送两条消息到test主题，并在发送完消息后等待10秒钟。
     * 这是为了给消费者足够的时间来接收和处理这些消息。
     */
    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("test", "你好");
        kafkaProducer.sendMessage("test", "在吗");

        try {
            Thread.sleep(1000 * 10);//10s
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Kafka生产者的实现类。它使用KafkaTemplate来发送消息到指定的主题。
 */
@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

/**
 * Kafka消费者的实现类。它使用@KafkaListener注解指定要监听的主题为test。
 * 当有新消息到达test主题时，handleMessage方法将被调用，并打印消息的内容。
 */
@Component
class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}