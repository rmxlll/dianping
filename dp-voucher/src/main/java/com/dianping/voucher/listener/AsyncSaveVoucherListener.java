package com.dianping.voucher.listener;

import com.alibaba.fastjson.JSON;

import com.dianping.voucher.domain.po.VoucherOrder;
import com.dianping.voucher.service.ISeckillVoucherService;
import com.dianping.voucher.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class AsyncSaveVoucherListener {
    @Resource
    private IVoucherOrderService voucherOrderService;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @RabbitListener(queuesToDeclare = {@Queue(name= "save")})
    public void AsyncSave(VoucherOrder voucherOrder)
    {
        log.info("接收到存储订单信息的消息,{}", JSON.toJSON(voucherOrder).toString());
        boolean success = seckillVoucherService.update().setSql("stock=stock-1").eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0).update();
        voucherOrderService.save(voucherOrder);
        log.info("订单信息存储完成?{}",success);
    }
}

