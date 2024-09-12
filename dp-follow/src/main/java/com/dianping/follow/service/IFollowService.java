package com.dianping.follow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.api.dto.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.follow.domain.po.Follow;
import com.dianping.follow.domain.vo.FollowVO;

import java.util.List;



public interface IFollowService extends IService<Follow> {

    Boolean follow(Long followUserId, Integer isFollow);

    Boolean isFollow(Long followUserId);

    List<UserDTO> followCommons(Long id);

    FollowVO followMine(Long id);

    FollowVO followYours(Long id);

    List<FollowVO> getFollowsByUserId(Long userId);
}
