package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件消费者
 */
@Component
public class EventConsumer implements CommunityConstant {

    // 日志记录器，用于输出日志信息。
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})//这里写漏了TOPIC_FOLLOW导致消费者不消费
    public void handleCommentMessage(ConsumerRecord record) {
        // 确保消息内容不为空
        if(record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        // 将消息记录的值解析为 Event 对象。如果解析失败，则记录错误并返回。
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("消息格式错误！");
            return;
        }

        // 创建一个 Message 对象，用于发送站内通知。设置发送者ID、接收者ID、对话ID、创建时间等字段。
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);//系统用户id默认为1
        message.setToId(event.getEntityUserId());//A给B点赞，系统发消息通知B
        message.setConversationId(event.getTopic());//私信存放123_234字符串，通知存放topic字符串
        message.setCreateTime(new Date());
        // 创建一个 content 的 Map 对象，用于存储通知的内容。将 Event 对象中的相关字段值存储到 content 中。
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if(!event.getData().isEmpty()) {
            for(Map.Entry<String,Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        //将message存到数据库中
        messageService.addMessage(message);
    }
}