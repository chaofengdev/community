package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
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
        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

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
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
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

}
