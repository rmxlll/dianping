package com.dianping.gateway.filter;

import com.dianping.gateway.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.dianping.common.utils.RedisConstants.LOGIN_USER_KEY;
import static com.dianping.common.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 实现GLobalFilter,Ordered，过滤请求
 * 弊端：只拦截当前请求，
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;

    private final StringRedisTemplate stringRedisTemplate;
    private final AntPathMatcher antPathMatcher= new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取request
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否进行登录校验,是否是所属服务接口，exclude直接放行
        if(isExclude(request.getPath().toString())){
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
        // 3. 获取token
        String token =null;
        List<String> headers = request.getHeaders().get("Authorization");
        if(headers !=null && !headers.isEmpty()){
            token = headers.get(0);
        }
        if (token==null){
            return chain.filter(exchange);
        }
        // 4. 校验并解析token
        String tokenKey = LOGIN_USER_KEY + token;
        try{
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(tokenKey);
            if(Objects.nonNull(entries)){
                stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
            }
        }catch (Exception e){
        //     拦截，设置响应码为401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    public Boolean isExclude(String path){
        for(String authPropertie:authProperties.getExcludePaths()){
            if(antPathMatcher.match(authPropertie,path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
