package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;//当前用户需要从HostHolde中取

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 用户上传头像功能。
     * MultipartFile是SpringMVC提供简化上传操作的工具类，一般来讲使用MultipartFile这个类主要是来实现以表单的形式进行文件上传功能。
     * 要想真正理解什么是MultipartFile，我们就需要从源码角度进行分析和理解。这里可以简单将其对象理解为上传的文件的抽象。
     * @param headerImage
     * @param model
     * @return
     */
    @RequestMapping(path = "/upload", method = RequestMethod.POST) //表单的提交方式必须是post
    public String uploadHeader(MultipartFile headerImage, Model model) {
        //判断是否有上传的图片
        if(headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }
        //判断上传的图片是否有正确的后缀名
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));//substring是左开右闭区间
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确，您上传的文件需要有正确的后缀名！");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径 File:An abstract representation of file and directory pathnames.
        File dest = new File(uploadPath + "/" + fileName);
        //将当前文件写入到目标文件中
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！" + e);//这里以后会统一处理，这里暂时略过。
        }
        //更新当前用户的头像路径（web访问路径）
        //http://localhost:8000/community/user/header/xxx.png
        User user = hostHolder.getUsers();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;//这里的路径是调用下面getHeader方法获取图片。
        userService.updateHeader(user.getId(), headerUrl);
        //return “/site/index”是返回一个模板路径，本次请求没有处理完，DispatcherServlet会将Model中的数据和对应的模板提交给模板引擎，让它继续处理完这次请求。
        //return "redirect:/index"是重定向，表示本次请求已经处理完毕，但是没有什么合适的数据展现给客户端，建议客户端再发一次请求，访问"/index"以获得合适的数据。
        return "redirect:/index";//重定向到首页，同时头像更新为最新的头像
    }

    /**
     * 从本地（服务器）读取图片并传给response对象（即可在浏览器展现）
     * 这里response对象底层没有进行进一步深入探究。？？？
     * 更新：关于“response.setContentType("image/"+suffix);”的解读
     * 这里主要是关于HTTP协议的内容，详细参见：https://www.runoob.com/http/http-tutorial.html
     * HTTP请求报文：请求行+请求头+空行+请求体；
     * HTTP响应报文：状态行+响应头+空行+响应体。
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) { //返回值void表示不是网页或者字符串，而是二进制字节流
        //服务器存放路径 这里的fileName是全限定名
        fileName = uploadPath + "/" + fileName;//输入流将从这里读取文件
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);//setContentType:Sets the content type of the response being sent to the client, if the response has not been committed yet.
        try(//这里需要复习一下IO流的知识点。详细知识点，视频参考：https://www.bilibili.com/video/BV1n3411Q7gi
            OutputStream os = response.getOutputStream();//输出流
            FileInputStream fis = new FileInputStream(fileName);//输入流 自己创建的需要手动关闭，但是放在这里，编译时会自动关闭。并且没有变量作用范围的烦恼。
        ) {
            byte[] buffer = new byte[1024];//缓冲区1024字节
            int b = 0;
            //fis.read(buffer)：the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached.
            while((b = fis.read(buffer)) != -1) {//Reads up to b.length bytes of data from this input stream into an array of bytes. This method blocks until some input is available.
                os.write(buffer, 0, b);//Writes len bytes from the specified byte array starting at offset off to this output stream.
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        } //不需要在finally关闭流，因为需要考虑变量作用范围。直接使用Java7的语法，java会自动帮我们关闭。
    }
}
