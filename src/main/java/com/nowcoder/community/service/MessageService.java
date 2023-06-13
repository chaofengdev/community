package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        //需要对敏感词先过滤
        //将特殊字符转换为对应的 HTML 实体编码，这样这些字符就会被正确地显示在 HTML 页面上，而不会被浏览器解释为 HTML 标签或脚本。
        //例如，将字符串 <script>alert('Hello, World!');</script> 传递给 HtmlUtils.htmlEscape 方法进行转义，将会把 < 转换为 &lt;，> 转换为 &gt;.以及 ' 转换为 &#39;，
        //得到转义后的字符串 &lt;script&gt;alert(&#39;Hello, World!&#39;);&lt;/script&gt;。
        //通过使用 HtmlUtils.htmlEscape 方法，可以确保在将用户输入内容展示在 HTML 页面上时，不会对页面结构或脚本产生意外的影响。
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    //将部分消息设置为已读
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);//`status`设置为1表示`已读`
    }
}
