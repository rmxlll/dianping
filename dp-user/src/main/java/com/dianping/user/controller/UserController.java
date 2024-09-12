package com.dianping.user.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dianping.common.utils.Result;
import com.dianping.user.domain.dto.LoginFormDTO;
import com.dianping.user.domain.po.User;
import com.dianping.user.domain.po.UserInfo;
import com.dianping.user.domain.vo.UserVO;
import com.dianping.user.service.IUserInfoService;
import com.dianping.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    // 使用RequiredArgsConstructor取代Autoweird，必须加final
    private final IUserService userService;

    private final IUserInfoService userInfoService;

    /**
     * 发送验证码
     * @param phone
     * @param
     * @return
     */
    @GetMapping("/code")
    public Boolean sendCode(@RequestParam("phone") String phone){
        log.info("Received request to send code for phone: {}", phone);
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public String login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        return userService.login(loginForm,session);
    }

    /**
     * 签到功能
     * @return
     */
    @PostMapping("/sign")
    public Boolean sign(){
        return userService.sign();
    }

    /**
     * 统计连续签到次数
     * @return
     */
    @PostMapping("/signCount")
    public Boolean signCount(){
        return userService.signCount();
    }

    /**
     * 实现登出功能
     * @return
     */
    @PostMapping("/logout")
    public Boolean logout(){
        return userService.logout();
    }

    /**
     * 修改昵称
     * @param nickName
     * @return
     */
    @PutMapping("/changeName")
    public Boolean changeName(@RequestParam("nickName") String nickName ){
        return userService.changeName(nickName);
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping("/info")
    public Result updateInfo(@RequestBody UserInfo user){
        return userInfoService.updateInfo(user);
    }

    /**
     * 获取需要查看的用户信息
     * @param id
     * @return
     */
    @GetMapping("/getUser")
    public UserVO getUser(@RequestParam("id")String id){
        User user = userService.getById(id);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return userVO;
    }

    /**
     * 获取当前用户的详细信息
     */
    @GetMapping("/getInfo")
    public Result getUserInfo(){
        return userInfoService.getUserInfo();
    }

    @PostMapping("/listByIds")
    public List<UserVO> listByIds(@RequestBody List<Long> userIds){
        List<User> users = userService.listByIds(userIds);
        List<UserVO> userVOs =users.stream().map(user -> BeanUtil.copyProperties(user, UserVO.class)).collect(Collectors.toList());
        return  userVOs;
    }

    @PostMapping("/list")
    public List<UserVO> list(@RequestBody QueryWrapper<UserVO> queryWrapper){
        List<User> users = userService.list(queryWrapper);
        List<UserVO> userVOs = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(Collectors.toList());
        return userVOs;
    }


}
