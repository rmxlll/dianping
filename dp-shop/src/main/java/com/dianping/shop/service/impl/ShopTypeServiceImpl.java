package com.dianping.shop.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.common.utils.Result;
import com.dianping.shop.domain.po.ShopType;
import com.dianping.shop.mapper.ShopTypeMapper;
import com.dianping.shop.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.dianping.common.utils.RedisConstants.*;


@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public List<ShopType> queryByList() {
        // 1.查询redis
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        List<ShopType> typeList = null;
        // 2.命中缓存，返回
        if (StrUtil.isNotBlank(shopTypeJson)){
            typeList = JSONUtil.toList(shopTypeJson,ShopType.class);
            return typeList;
        } else if(Objects.nonNull(shopTypeJson)){
            return null;
        }
        // 3.未命中，查询数据库
        typeList = this.list(new LambdaQueryWrapper<ShopType>().orderByAsc(ShopType::getSort));
        if(Objects.isNull(typeList)){
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY,"",CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_TYPE_KEY,JSONUtil.toJsonStr(typeList),CACHE_SHOP_TTL,TimeUnit.MINUTES);
        return typeList;
    }
}
