package com.dianping.voucher.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.utils.Result;
import com.dianping.voucher.domain.po.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    Long createVoucherOrder( Long voucherId);
}
