package com.nowcoder.community.entity;

import java.util.Date;

/**
 * 消息的实体类 这里的消息有两类：私信、系统通知
 * 私信：用户A->用户B 其中AB均为普通用户
 * 通知：用户A->用户B 其中A限定为系统用户，用户id为1
 */
public class Message {
    //私信id
    private int id;

    //发信人id
    private int fromId;

    //收信人id
    private int toId;

    //如果是私信：发私信人_收私信人、收私信人_发私信人
    //如果是通知：comment、like、follow
    private String conversationId;

    //内容
    //如果是私信：普通文本，如“很有帮助。偶尔有错漏。”
    //如果是通知：Json字符串，如“{"entityType":1,"entityId":237,"postId":237,"userId":112}”
    //ps：json字符串部分符号转义，则数据库保存的是“{&quot;entityType&quot;:1,&quot;entityId&quot;:234,&quot;postId&quot;:234,&quot;userId&quot;:162}”
    private String content;

    //私信是否已读
    //0未读 1已读 2失效
    private int status;

    //创建时间
    private Date createTime;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                ", conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
