package com.dianping.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.user.domain.dto.LoginFormDTO;
import com.dianping.user.domain.po.User;
import com.dianping.user.domain.vo.UserVO;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface IUserService extends IService<User> {
    Boolean sendCode(String phone);
    String login(LoginFormDTO loginForm, HttpSession session);
    Boolean sign();

    Boolean signCount();

    Boolean logout();


    Boolean changeName(String nickName);


    UserDTO getMe();

    List<User> list(QueryWrapper<UserVO> queryWrapper);
}
