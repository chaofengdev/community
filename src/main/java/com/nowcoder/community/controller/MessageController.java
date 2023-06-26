package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        //Integer.valueOf("abc");//验证异常的错误代码
        User user = hostHolder.getUsers();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表--这里需要什么数据，取决于前端需要展现哪些数据。这点需要理解。这里需要的数据有点多，需要对数据表理解深刻一点。
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null) {
            for(Message message : conversationList) {
                Map<String,Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));//某个会话私信数量
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));//某个会话的未读私信数量
                int targetId = (user.getId() == message.getFromId()) ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));//发私信或收私信的用户
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);
        //查询未读私信数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询未读通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    //私信详情
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        //私信列表
        List<Message> lettersList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(lettersList != null) {
            for(Message message : lettersList) {
                Map<String,Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //将未读消息提取出来，设置为已读
        List<Integer> ids = getLetterIds(lettersList);//这里lettersList是当前分页查询出来某个会话的部分私信
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        //将"/site/letter-detail"解析为具体的视图模板文件路径，然后进行渲染。最终生成的内容将作为HTTP响应的主体返回给客户端。--这里深刻理解需要查看springmvc源码，留待日后讨论。
        return "/site/letter-detail";
    }

    //辅助方法，根据conversationId找到会话目标。
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUsers().getId() == id0) {
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    //辅助方法：将集合中所有消息中的未读消息的id提取到新集合中
    //该方法看起来很怪，其实是为了提取未读消息的id，再根据id将这些未读消息设置成已读，
    //即将message中的status字段改为1表示已读。总之该方法是个辅助方法，简化了主controller方法中的逻辑表达。
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            for(Message message : letterList) {
                //只有当前用户是收信人并且该消息是未读，才将该消息添加到集合中
                if(hostHolder.getUsers().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------
     * ps：这里对ResponseBody注解进行解释说明。
     *
     * 在使用Spring MVC或Spring WebFlux进行Web开发时，通常需要将方法的返回值转换为HTTP响应返回给客户端。
     * 通常情况下，方法的返回值会被封装为视图模型或模板，然后由视图解析器进行解析和渲染，最终生成HTML等内容返回给客户端。
     * 然而，在某些情况下，我们希望直接将方法的返回值作为HTTP响应的内容返回，而不经过视图解析器的处理。这时就可以使用@ResponseBody注解。
     * ResponseBody注解可以应用在方法级别或控制器类级别上。当应用在方法上时，它会告诉Spring框架将方法的返回值直接写入HTTP响应的主体部分，而不进行视图解析和渲染。
     * 返回值可以是任意类型的对象，Spring会根据请求的Content-Type头部信息选择适当的转换器，将返回值转换为对应的数据格式，例如JSON、XML等。
     *
     * 需要注意的是，如果使用@RestController注解来代替@Controller注解，则所有的方法默认都会被@ResponseBody注解修饰，
     * 因为@RestController注解本身就是@ResponseBody和@Controller的组合注解。
     * -------------------------------------------------------------------------------------------------------------
     */
    //发送私信
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        //Integer.valueOf("abc");//验证异常的错误代码
        //判断目标用户是否存在
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！请务必输入正确的用户名。");
        }
        //根据前端传入信息，创建message
        Message message = new Message();
        message.setFromId(hostHolder.getUsers().getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);//`0`表示未读，`1`表示已读。
        if(message.getFromId() < message.getToId()) {//111_222 id小的在前面，数据库的设计规则
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        //插入message
        messageService.addMessage(message);
        //返回成功插入的json信息
        return CommunityUtil.getJSONString(0);//0表示发送成功
    }

    //通知列表--三种通知存在部分代码复制粘贴的问题，需要重构的部分。
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUsers();

        //查询评论类通知
        Message message_comment = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if(message_comment != null) {
            Map<String,Object> messageVO = new HashMap<>();//被模板引用并显示数据的对象
            messageVO.put("message", message_comment);

            //HtmlUtils.htmlUnescape 方法用于将这些 HTML 实体编码的文本转换回普通文本格式。它会将`&quot;`转换回`"`
            //为什么需要将特定字符转义成实体编码？
            //当这些特定字符需要在 HTML 内容中作为普通文本而不是标签或属性进行显示时，就需要进行转义或编码。这样做是为了避免与 HTML 结构产生冲突。
            //数据库中message表的content字段，为什么是“{&quot;entityType&quot;:1,&quot;entityId&quot;:234,&quot;postId&quot;:234,&quot;userId&quot;:162}”
            //可能是出于安全考虑？？--这里有疑问，感觉这里不是表单，没有必要转义。
            String content = HtmlUtils.htmlUnescape(message_comment.getContent());//将实体编码转义成特定字符，恢复json数据
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);//json字符串转成map集合

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));//A评论B，系统通知B，这个userId是A的id
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }


        //查询点赞类通知
        Message message_like = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if(message_like != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message", message_like);

            String content = HtmlUtils.htmlUnescape(message_like.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            model.addAttribute("likeNotice", messageVO);
        }

        //查询关注类通知
        Message message_follow = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if(message_like != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message", message_follow);

            String content = HtmlUtils.htmlUnescape(message_follow.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            //messageVO.put("postId", data.get("postId"));//业务逻辑中关注某个用户不需要帖子信息

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice", messageVO);
        }

        //查询未读消息数量
        //未读朋友私信
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //未读系统通知
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    //通知详情
    /**
     * 该方法的作用是根据指定主题(topic)获取用户的通知详情，并将相关数据存储在Model中供视图渲染使用。
     * 同时，该方法还负责将用户未读的通知消息标记为已读。
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUsers();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());//分页查询通知消息
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if(noticeList != null) {
            for(Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));//HashMap的get(key)方法获取指定键的值时，如果该键不存在于HashMap中，get()方法将返回null。
                //通知作者-这里通知的作者其实是系统管理员
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVOList.add(map);
            }
            model.addAttribute("notices", noticeVOList);
        }

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

}
