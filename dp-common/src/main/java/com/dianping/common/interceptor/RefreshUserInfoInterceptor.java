package com.dianping.common.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.dianping.common.utils.RedisConstants.LOGIN_USER_KEY;

public class RefreshUserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的用户信息
        String userJson = request.getHeader("user-info");
        if (StrUtil.isBlank(userJson)) {
            return true;
        }
        // 存入用户信息
        if(userJson != null){
            UserDTO userDTO = JSON.parseObject(userJson, UserDTO.class);
            UserHolder.saveUser(userDTO);
        }
        // 3.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
