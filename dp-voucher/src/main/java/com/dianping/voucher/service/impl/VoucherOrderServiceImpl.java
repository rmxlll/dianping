package com.dianping.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.voucher.exception.OrderException;
import com.dianping.voucher.utils.RedisIdWorker;
import com.dianping.common.utils.Result;
import com.dianping.common.utils.UserHolder;
import com.dianping.voucher.domain.po.SeckillVoucher;
import com.dianping.voucher.domain.po.VoucherOrder;
import com.dianping.voucher.mapper.VoucherOrderMapper;
import com.dianping.voucher.service.ISeckillVoucherService;
import com.dianping.voucher.service.IVoucherOrderService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

import static com.dianping.common.utils.RedisConstants.LOCK_ORDER_KEY;
import static com.dianping.common.utils.RedisConstants.SECKILL_VOUCHER_ORDER;


@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Autowired
    private ISeckillVoucherService seckillVoucherService;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }



    @Transactional
    @Override
    public Long createVoucherOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        RLock lock = redissonClient.getLock(LOCK_ORDER_KEY + userId);
        boolean isLock = lock.tryLock();
        if(!isLock){
            throw new OrderException("不允许重复下单！");
            // return Result.fail("不允许重复下单！");
        }
        try {
            // 1、判断当前用户是否是第一单
            int count = this.count(new LambdaQueryWrapper<VoucherOrder>()
                    .eq(VoucherOrder::getUserId, userId));
            if (count >= 1) {
                // 当前用户不是第一单
                // return Result.fail("用户已购买");
                throw new OrderException("用户已购买");
            }

            // 2、用户是第一单，可以下单，秒杀券库存数量减一
            boolean flag = seckillVoucherService.update(new LambdaUpdateWrapper<SeckillVoucher>()
                    .eq(SeckillVoucher::getVoucherId, voucherId)
                    .gt(SeckillVoucher::getStock, 0)
                    .setSql("stock = stock -1"));
            if (!flag) {
                throw new OrderException("秒杀券扣减失败");
            }
            // 3、创建对应的订单，并保存到数据库
            VoucherOrder voucherOrder = new VoucherOrder();
            long orderId = redisIdWorker.nextId(SECKILL_VOUCHER_ORDER);
            voucherOrder.setId(orderId);
            voucherOrder.setUserId(UserHolder.getUser().getId());
            voucherOrder.setVoucherId(voucherOrder.getId());
            flag = this.save(voucherOrder);
            if (!flag) {
                throw new OrderException("创建秒杀券订单失败");
            }

            // 4、返回订单id
            return orderId;
        }catch (Exception e){
            throw new RuntimeException();
        }finally {
            lock.unlock();
        }

    }


}
