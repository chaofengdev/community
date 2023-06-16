package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant{
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    /**
     * 获取首页帖子，分页展示
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) { //前端需要Page对象的相关数据，实现分页效果
        //方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model，
        //所以，在thymeleaf中可以直接访问Page对象中的数据，省略了model.addAttribute("page",page);的过程
        page.setRows(discussPostService.findDiscussPostRows(0));//page中保存了目前的总行数
        page.setPath("/index");//page中保存了访问地址

        //查询获得帖子集合   当前起始行号和每页数量都从page实例中获取
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());

        //discussPosts集合保存了若干个map，每个map保存帖子对象和用户对象，这个discussPosts集合会被返回到前端中
        List<Map<String,Object>> discussPosts = new ArrayList<>();

        if(list != null) {
            for(DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                //帖子对象
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                //用户对象
                map.put("user",user);
                //新增：帖子点赞相关
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);//缺少这个前端不展示帖子信息，因为discussPosts中不存在map
            }
        }

        //添加到model中，返回给前端  这里的discussPosts表示一个集合，集合中保存了若干个map，map中保存了每个帖子对象和用户，前端可以拿到这个集合，并进行相关处理
        model.addAttribute("discussPosts", discussPosts);
        //model.addAttribute("page",page); //这里自动添加，不需要手动添加
        //视图名index
        return "index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }
}
