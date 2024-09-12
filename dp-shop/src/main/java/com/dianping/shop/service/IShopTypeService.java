package com.dianping.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.utils.Result;
import com.dianping.shop.domain.po.ShopType;

import java.util.List;

public interface IShopTypeService extends IService<ShopType> {
    List<ShopType> queryByList();
}
