package com.dianping.follow.controller;


import com.dianping.api.dto.UserDTO;
import com.dianping.common.utils.Result;
import com.dianping.follow.domain.vo.FollowVO;
import com.dianping.follow.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Boolean follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Integer isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Boolean isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    @GetMapping("/common/{id}")
    public List<UserDTO> followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }

    @GetMapping("/mine/{id}")
    public FollowVO followMine(@PathVariable("id") Long id){
        return followService.followMine(id);
    }

    @GetMapping("/yours/{id}")
    public FollowVO followYours(@PathVariable("id") Long id){
        return followService.followYours(id);
    }


    @GetMapping("/follows")
    public List<FollowVO> getFollowsByUserId(@RequestParam("userId") Long userId) {
        List<FollowVO> follows = followService.getFollowsByUserId(userId);
        return follows;
    }
}
