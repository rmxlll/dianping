package com.dianping.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.utils.Result;
import com.dianping.user.domain.po.UserInfo;



public interface IUserInfoService extends IService<UserInfo> {

    Result updateInfo(UserInfo user);

    Result getUserInfo();

}
