package com.dianping.api.client;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.api.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("dp-user")
public interface UserClient {
    @PostMapping("/user/listByIds")
    List<UserVO> listByIds(@RequestBody List<Long> userIds);

    @GetMapping("/user/getUser")
    UserVO getUser(@RequestParam("id") Long id);

    @RequestMapping(value = "/user/list", method = RequestMethod.POST)
    List<UserVO> list(@RequestBody LambdaQueryWrapper<UserVO> queryWrapper);
}
