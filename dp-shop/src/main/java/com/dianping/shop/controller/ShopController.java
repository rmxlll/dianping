package com.dianping.shop.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.dianping.common.utils.Result;
import com.dianping.common.utils.SystemConstants;
import com.dianping.shop.domain.po.Shop;
import com.dianping.shop.service.IShopService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/{id}")
    public Shop queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * 新增商铺信息
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping("/saveShop")
    public Boolean saveShop(@RequestBody Shop shop) {
        return shopService.save(shop);
    }

    /**
     * 更新商铺信息
     * @param shop 商铺数据
     * @return 无
     */
    @PutMapping("/updateShop")
    public Boolean updateShop(@RequestBody Shop shop) {
        // 写入数据库
        return shopService.updateId(shop);
    }

    /**
     * 根据商铺类型分页查询商铺信息
     * @param typeId 商铺类型
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/type")
    public List<Shop> queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y,
            @RequestParam(value = "sortBy", required = false) String sortBy
    ) {
        return shopService.queryShopByType(typeId, current, x, y,sortBy);
    }

    /**
     * 根据商铺名称关键字分页查询商铺信息
     * @param name 商铺名称关键字
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/name")
    public List<Shop> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return page.getRecords();
    }

    @PostMapping("/list")
    public Result getShopList(){
        List<Shop> list = shopService.list();
        return Result.ok(list);
    }

    @GetMapping("/check/{name}")
    public Shop getByName(@PathVariable("name")String name){
        if (name != null && !name.isEmpty()) {
            // 根据name查询店铺
            Shop shop = shopService.query().eq("name", name).one();
            return shop;
        } else {
            // 根据id查询店铺
            return null;
        }
    }
}
