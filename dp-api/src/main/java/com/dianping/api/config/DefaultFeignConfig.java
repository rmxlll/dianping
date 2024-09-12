package com.dianping.api.config;

import com.alibaba.fastjson.JSON;
import com.dianping.common.UserDTO;
import com.dianping.common.utils.UserHolder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    /**
     * 问题：在微服务传递时，没有传递用户信息
     * 解决方法：在微服务调用时，把用户信息存入请求头
     * @return
     */
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                UserDTO userDTO = UserHolder.getUser();
                if(userDTO!=null){
                    String userJson = JSON.toJSONString(userDTO);
                    requestTemplate.header("user-info",userJson);
                }
            }
        };
    }
}
