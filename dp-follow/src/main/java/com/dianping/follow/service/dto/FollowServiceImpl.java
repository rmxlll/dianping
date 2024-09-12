package com.dianping.follow.service.dto;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.api.client.UserClient;
import com.dianping.api.dto.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.UserHolder;
import com.dianping.follow.domain.po.Follow;
import com.dianping.follow.domain.vo.FollowVO;
import com.dianping.follow.mapper.FollowMapper;
import com.dianping.follow.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dianping.common.utils.RedisConstants.FOLLOW_KEY;


@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserClient userClient;
    @Override
    public Boolean follow(Long followUserId, Integer isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key = FOLLOW_KEY + userId;
        Boolean f = isFollow>0? true:false;
        if(f){
            Follow follow =new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = this.save(follow);
            if(isSuccess){
                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        } else {
            boolean isSuccess = this.remove(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getUserId, userId)
                    .eq(Follow::getFollowUserId, followUserId));
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }

        }
        return true;
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        return null;
    }

    @Override
    public List<UserDTO> followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key1 = FOLLOW_KEY + userId;
        String key2 = FOLLOW_KEY + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1,key2);
        if (Objects.isNull(intersect) || intersect.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 查询共同关注的用户信息
        List<UserDTO> userDTOList = userClient.listByIds(ids).stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return userDTOList;
    }

    @Override
    public FollowVO followMine(Long id) {
        return null;
    }

    @Override
    public FollowVO followYours(Long id) {
        return null;
    }

    @Override
    public List<FollowVO> getFollowsByUserId(Long userId) {
        List<Follow> follows = this.baseMapper.selectList(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowUserId, userId));
        List<FollowVO> followVOS = follows.stream().map(follow -> BeanUtil.copyProperties(follow, FollowVO.class)).collect(Collectors.toList());
        return followVOS;
    }
}
