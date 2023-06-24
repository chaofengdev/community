package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件对象--理解对比只用字符串test作为主题，这种结构化方式的好处
 * Event对象可以在Kafka中以结构化的方式传递，并且消费者可以根据需要解析和处理事件数据。
 * 这种灵活性使得可以根据具体业务需求将任意类型的事件发布到Kafka中，并能够将事件与特定用户、实体和相关数据关联起来。
 */
public class Event {//事件对象，用于在消息队列中传递和处理数据。

    private String topic;//事件所属的主题

    private int userId;//与`事件`相关的用户ID

    private int entityType;//事件涉及的实体类型

    private int entityId;//事件涉及的实体ID

    private int entityUserId;//与`实体`相关的用户ID

    private Map<String, Object> data = new HashMap<>();//存储事件数据的Map对象。这里主要用于前端展现相关数据。

    public String getTopic() {
        return topic;
    }

    /**
     * 该方法允许在一个语句中同时设置topic字段，并将更新后的对象返回，以便可以在后续代码中继续对该对象进行其他设置或操作。
     * 通过连续调用setXXX方法，我们可以在一行代码中设置topic、userId、entityId和data字段，并将最终更新后的Event对象赋值给event变量。
     * 这种方法链式调用的编程风格在构建复杂对象时特别有用，因为它可以减少代码的冗余性，并提供更清晰、易读的代码结构。
     * @param topic
     * @return
     */
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Event event = new Event().setData(key1,value1).setData(key2,value2);
     * @param key
     * @param value
     * @return
     */
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", entityUserId=" + entityUserId +
                ", data=" + data +
                '}';
    }
}
