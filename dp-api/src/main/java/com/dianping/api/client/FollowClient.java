package com.dianping.api.client;

import com.dianping.api.vo.FollowVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("dp-follow")
public interface FollowClient {
    @GetMapping("/follows")
    List<FollowVO> getFollowsByUserId(@RequestParam("userId") Long userId);

}
