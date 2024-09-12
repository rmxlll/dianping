package com.dianping.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dianping.common.utils.Result;
import com.dianping.voucher.domain.po.SeckillVoucher;
import com.dianping.voucher.domain.po.Voucher;
import com.dianping.voucher.mapper.VoucherMapper;
import com.dianping.voucher.service.ISeckillVoucherService;
import com.dianping.voucher.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.dianping.common.utils.RedisConstants.SECKILL_STOCK_KEY;


@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Boolean queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = this.query().eq("shop_id",shopId).list();
        if(Objects.isNull(vouchers)){
            return false;
        }
        return true;
    }

    @Override
    public boolean addSeckillVoucher(Voucher voucher) {
        Boolean isSuccess1 =this.save(voucher);
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        Boolean isSuccess2 = seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY+voucher.getId(),voucher.getStock().toString());
        if(!isSuccess1&&!isSuccess2){
            return false;
        }
        return true;
    }
}
