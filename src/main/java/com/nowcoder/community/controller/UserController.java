package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Host;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController implements CommunityConstant {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String getUploadHeader(MultipartFile headerImg, Model model){
        if (headerImg==null){
            model.addAttribute("error","您还没有选择图片!");
            return "/site/setting";
        }
        //获取上传文件原始名
        String originalFilename = headerImg.getOriginalFilename();
        //判断文件名非空
        if (originalFilename != null) {
            //获取文件后缀
            String[] splitArr = originalFilename.split("\\.");
            String suffix = splitArr[splitArr.length - 1];
            if (suffix==null){
                model.addAttribute("error","您上传的图片格式不正确!");
                return "/site/setting";
            }else {
                String filename = CommunityUtil.getUUID() +"."+ suffix;
                File destination = new File(uploadPath +"/"+ filename);
                try {
                    headerImg.transferTo(destination);
                } catch (IOException e) {
                    log.error("上传文件失败:{}",e.getMessage());
                    throw new RuntimeException("上传文件失败,服务器发生异常!");
                }
                String webUrl = domain + contextPath + "/user/header/" + filename;
                User user = hostHolder.getUser();
                userService.updateHeader(user.getId(),webUrl);
                model.addAttribute("success","上传成功!");
            }
        }
        //生成随机文件名
        return "redirect:/index";
    }
    /**
     * 获取头像
     * @param filename
     * @param response
     */
    @RequestMapping(path = "/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable String filename, HttpServletResponse response){
        //服务器存放的路径
        filename=uploadPath +"/"+ filename;
        String[] splitArr = filename.split("\\.");
        String suffix = splitArr[splitArr.length - 1];
        response.setContentType("image/"+suffix);
        try (FileInputStream fileInputStream=new FileInputStream(filename)){
            ServletOutputStream outputStream = response.getOutputStream();
            byte[]bytes=new byte[1024];
            int b=0;
            while ((b= fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,b);
            }
        } catch (IOException e) {
            log.error("读取头像失败:{}",e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/passwordChange",method = RequestMethod.POST)
    public String passwordChange(@CookieValue("ticket")String ticket ,String oldPassword, String newPassword, String confirmNewPassword, Model model){
        if (StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldError","原密码不能为空!");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword)){
            model.addAttribute("newError","新密码不能为空!");
            return "/site/setting";
        }
        if (StringUtils.isBlank(confirmNewPassword)){
            model.addAttribute("confirmError","确认密码不能为空!");
            return "/site/setting";
        }
        if (!newPassword.equals(confirmNewPassword)){
            model.addAttribute("confirmError","两次输入的密码不一致!");
            return "/site/setting";
        }
        if (oldPassword.equals(newPassword)){
            model.addAttribute("newError","新密码不能与旧密码相同!");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if (user==null){
            throw new NullPointerException("用户为null");
        }
        String password = user.getPassword();
        String salt = user.getSalt();
        if (!password.equals(CommunityUtil.md5(oldPassword+salt))){
            model.addAttribute("oldError","原密码错误!");
            return "/site/setting";
        }
        userService.changePassword(ticket,newPassword,user);
        return "redirect:/login";
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if (user==null){
            throw new RuntimeException("用户不存在!");
        }
        //用户基本信息
        model.addAttribute("user",user);
        int likeCount =likeService.selectLikeCount(user.getId());
        //用户点赞数
        model.addAttribute("likeCount",likeCount);
        //用户关注数
        Long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //用户粉丝数
        Long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否关注
        Boolean isFollow=false;
        if (hostHolder.getUser()!=null){
           isFollow = followService.isFollow(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
            model.addAttribute("isFollow",isFollow);
        return "/site/profile";
    }
}
