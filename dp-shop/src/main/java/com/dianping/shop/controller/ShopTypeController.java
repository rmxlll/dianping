package com.dianping.shop.controller;


import com.dianping.common.utils.Result;
import com.dianping.shop.domain.po.ShopType;
import com.dianping.shop.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    /**
     * 查看店铺类型
     * @return
     */
    @GetMapping("list")
    public List<ShopType> queryTypeList() {
         return typeService.queryByList();
    }
}
