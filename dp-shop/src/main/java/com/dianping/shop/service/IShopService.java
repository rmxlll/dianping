package com.dianping.shop.service;

import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.utils.Result;
import com.dianping.shop.domain.po.Shop;

import java.util.List;

public interface IShopService extends IService<Shop> {
    Shop queryById(Long id);

    Boolean updateId(Shop shop);

    List<Shop> queryShopByType(Integer typeId, Integer current, Double x, Double y, String sortBy);

}
