package com.dianping.shop.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.common.utils.Result;
import com.dianping.common.utils.SystemConstants;
import com.dianping.shop.domain.po.Shop;
import com.dianping.shop.mapper.ShopMapper;
import com.dianping.shop.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dianping.common.utils.RedisConstants.*;


@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据id查询商铺缓存（涉及缓存穿透，缓存击穿（互斥锁））
     * @param id
     * @return
     */
    @Override
    public Shop queryById(Long id) {
        // 1.查询redis中的店铺缓存
        String key = CACHE_SHOP_KEY + id;
        Shop shop = getShopFromCache(key);
        if (Objects.nonNull(shop)){
            return shop;
        }
        // 防止缓存击穿
        try{
            String lockKey= LOCK_SHOP_KEY+id;
            boolean isLock = tryLock(lockKey);
            if(!isLock){
                // 2.1 获取锁失败，已有线程在重建缓存，则休眠重试
                Thread.sleep(50);
                return queryById(id);
            }
            // 2.2 获取锁成功，判断缓存是否重建，防止堆积的线程全部请求数据库（所以说双检是很有必要的）
            shop = getShopFromCache(key);
            if (Objects.nonNull(shop)) {
                // 缓存命中，直接返回
                return shop;
            }
            Shop shop1 = this.getById(id);
            if (Objects.isNull(shop1)){
                // 数据库也为空，防止缓存穿透，redis缓存空值
                stringRedisTemplate.opsForValue().set(key,"", CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }
            // TODO 未保存redis，有bug
            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop1),CACHE_SHOP_TTL,TimeUnit.MINUTES);
            return shop1;

        } catch (Exception e) {
            throw new RuntimeException("发生异常");
        } finally {
            unlock(key);
        }

    }

    /**
     * 更新商铺数据（涉及数据一致性）
     * @param shop
     * @return
     */
    @Transactional
    @Override
    public Boolean updateId(Shop shop) {
        // 1.更新数据库
        boolean f = this.updateById(shop);
        // 2.如果更新失败，抛出异常，事务回滚
        if (!f){
            throw new RuntimeException("数据库更新失败");
        }
        // 3.删除缓存
        f = stringRedisTemplate.delete(CACHE_SHOP_KEY+shop.getId());
        // 4.缓存删除失败，抛出异常，事务回滚
        if(!f){
            throw new RuntimeException("缓存删除失败");
        }
        return true;
    }

    @Override
    public List<Shop> queryShopByType(Integer typeId, Integer current, Double x, Double y, String sortBy) {
        //1. 判断是否需要根据距离查询
        if (x == null || y == null) {
            // 根据类型分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return page.getRecords();
        }
//        以下是需要根据距离查询

        //2. 计算分页查询参数
        int from = (current - 1) * SystemConstants.MAX_PAGE_SIZE;
        int end = current * SystemConstants.MAX_PAGE_SIZE;


        String key = SHOP_GEO_KEY + typeId;
        //3. 查询redis、按照距离排序、分页; 结果：shopId、distance
        //GEOSEARCH key FROMLONLAT x y BYRADIUS 5000 m WITHDIST
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end));

        if (results == null) {
            return Collections.emptyList();
        }

        //4. 解析出id
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();

        if (list.size() < from) {
            //起始查询位置大于数据总量，则说明没数据了，返回空集合
            return Collections.emptyList();
        }

        ArrayList<Long> ids = new ArrayList<>(list.size());
        HashMap<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });


        //5. 根据id查询shop
        String idsStr = StrUtil.join(",", ids);

        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD( id," + idsStr + ")").list();
        for (Shop shop : shops) {
            //设置shop的举例属性，从distanceMap中根据shopId查询
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        //6. 返回
        return shops;
    }

    private Shop getShopFromCache(String key) {
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 缓存数据有值，说明缓存命中了，直接返回店铺数据
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断缓存中查询的数据是否是空字符串(isNotBlank把 null 和 空字符串 给排除了)
        if (Objects.nonNull(shopJson)) {
            // 当前数据是空字符串，说明缓存也命中了（该数据是之前缓存的空对象），直接返回失败信息
            return null;
        }
        // 缓存未命中（缓存数据既没有值，又不是空字符串）
        return null;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 拆箱要判空，防止NPE
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}