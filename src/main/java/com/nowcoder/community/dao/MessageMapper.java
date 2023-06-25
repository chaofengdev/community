package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    /**
     *=======================================
     * 查询会话及私信
     *=======================================
     */
    // 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    //这里需要查总的未读私信数量，或者某个会话的未读私信数量，需要动态拼接sql
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     *=======================================
     * 新增会话或私信
     *=======================================
     */
    //新增消息
    int insertMessage(Message message);

    /**
     *=======================================
     * 修改会话或私信
     *=======================================
     */
    //（批量）修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    /**
     *=======================================
     * 查询某个主题下最新的通知
     *=======================================
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     *=======================================
     * 查询某个主题下通知的数量
     *=======================================
     */
    int selectNoticeCount(int userId, String topic);

    /**
     *=======================================
     * 查询未读的通知的数量
     *=======================================
     */
    int selectNoticeUnreadCount(int userId, String topic);
}
