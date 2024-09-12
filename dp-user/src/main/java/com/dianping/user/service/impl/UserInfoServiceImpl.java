package com.dianping.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.common.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.UserHolder;
import com.dianping.user.domain.po.UserInfo;
import com.dianping.user.mapper.UserInfoMapper;
import com.dianping.user.service.IUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;



@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Resource
    UserInfoMapper userInfoMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    public Result updateInfo(UserInfo user) {
        UserDTO loginUser = UserHolder.getUser();
        Long userId = loginUser.getId();

        if (userId == null) {
            throw new RuntimeException();
        }
        UserInfo userInfo = this.query().eq("user_id", user.getUserId()).one();
        if (userInfo == null) {
            userInfo = user;
            userInfoMapper.insert(userInfo);
            if (user.getBirthday()!=null && !user.getBirthday().equals("")){
                userInfo.setBirthday(user.getBirthday().plusDays(1)); // 将生日加上一天

            }
            updateById(userInfo);
            return Result.ok(userInfo);
        }
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getUserId,userInfo.getUserId());
        if (user.getBirthday()!=null && !user.getBirthday().equals("")){
            user.setBirthday(user.getBirthday().plusDays(1));// 将生日加上一天
        }
         // 将生日加上一天
        update(user,queryWrapper);
        //2.删除缓存
        String key = "user:info"+userId;
        stringRedisTemplate.delete(key);
        return Result.ok(userInfo);
    }

    @Override
    public Result getUserInfo() {
        // 1.获取当前用户id
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        if (userId == null){
            throw new RuntimeException();
        }
        // 2.查找redis，当前用户id
        String key = "user:info"+userId;
        String cache = stringRedisTemplate.opsForValue().get(key);
        UserInfo userInfo = null;
        // 3.如果为空
        if (cache!=null){
            userInfo = JSONUtil.toBean(cache, UserInfo.class);
            return Result.ok(userInfo);
        } else {
            userInfo = this.query().eq("user_id", userId).one();
            if (userInfo == null) {
                return Result.ok();
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(userInfo));
        }
        return Result.ok(userInfo);
    }

}
