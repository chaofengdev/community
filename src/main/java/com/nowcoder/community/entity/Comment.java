package com.nowcoder.community.entity;

import java.util.Date;

/**
 * Comment实体类
 * 对帖子的评论叫做评论comment
 * 对评论的评论叫做回复reply
 *
 */
public class Comment {

    //评论的ID
    private int id;

    //用户ID，表示评论的发表者
    private int userId;

    //实体类型，表示评论所涉及的对象类型
    //实体类型可以是各种不同的对象，例如用户、文章、问题等。具体的实体类型可能通过预先定义的常量或枚举值来表示，每个值与特定类型的实体相关联。
    private int entityType;

    //实体ID，表示评论所针对的具体对象的标识
    //它用于唯一标识被评论的实体，如用户的ID、文章的ID、问题的ID等。通过此标识，可以将评论与特定的实体关联起来，从而可以在系统中定位和检索相关评论。
    private int entityId;

    //目标ID，表示评论所针对的目标对象的标识
    //它用于标识评论的目标对象，这个目标对象可以是评论针对的另一个实体，如回复某个评论或回复某个用户的评论。
    //通过这个字段，可以建立评论与目标对象之间的关联，实现评论的层级结构或对特定评论的回复功能。
    //例子：entityId为ENTITY_TYPE_COMMENT表示帖子下的评论，entityId表示具体是哪个评论，targetId表示具体对哪个用户评论
    private int targetId;

    // 评论内容
    private String content;

    // 评论状态，表示评论的当前状态
    private int status;

    // 创建时间，表示评论的创建时间
    private Date createTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
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

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
