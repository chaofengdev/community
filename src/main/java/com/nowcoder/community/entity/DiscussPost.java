package com.nowcoder.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * 实体类：帖子
 */

/**
 * Elasticsearch中定义索引设置的注解@Document
 * indexName：指定文档将存储在的索引的名称，这里是 "discusspost"。
 * type：定义文档的类型。在Elasticsearch中，每个文档都属于一个类型，但从Elasticsearch 6.x开始，类型已经被弃用，并且可能在未来的版本中被移除。"_doc"类型是一个常见的约定，用于表示默认的文档类型。
 * shards：指定索引将被划分为的主分片数量。分片是Elasticsearch中用于将数据分布到多个节点以实现可扩展性和性能的技术。在这个例子中，索引将有6个主分片。
 * replicas：为每个主分片指定要创建的副本分片数量。副本分片提供冗余性，并通过允许并行操作来提高搜索性能。在这个例子中，每个主分片将有3个副本分片。
 */
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {
    //帖子id
    /**
     * 用于标识实体类中作为文档标识符的字段。
     * 即用于指定实体类中哪个字段将被映射为elasticsearch文档的唯一标识符。
     */
    @Id
    private int id;

    //帖子发帖人id
    @Field(type = FieldType.Integer)
    private int userId;

    //帖子标题
    /**
     * 注解@Field：在实体类中定义字段的映射信息，自动生成相应的索引映射
     * type：指定字段的类型。在这种情况下，字段类型被设置为FieldType.Text，表示文本类型的字段。
     * analyzer：指定索引分析器。在这个例子中，使用的分析器是"ik_max_word"，它是一个中文分词器，将文本分解为最大长度的词语进行索引。
     * searchAnalyzer：指定搜索分析器。在这个例子中，使用的搜索分析器是"ik_smart"，它是一个中文分词器，采用较智能的方式进行查询文本的分析。
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")//ik_max_word和ik_smart是分词器的名字。
    private String title;

    //帖子内容
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    //帖子类型
    @Field(type = FieldType.Integer)
    private int type;

    //帖子状态
    @Field(type = FieldType.Integer)
    private int status;

    //帖子创建时间
    @Field(type = FieldType.Date)
    private Date createTime;

    //帖子评论数量
    @Field(type = FieldType.Integer)
    private int commentCount;

    //帖子的权值
    @Field(type = FieldType.Double)
    private double score;


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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DisscussPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }
}
