package com.dianping.voucher.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.common.utils.Result;
import com.dianping.voucher.domain.po.Voucher;


public interface IVoucherService extends IService<Voucher> {

    Boolean queryVoucherOfShop(Long shopId);

    boolean addSeckillVoucher(Voucher voucher);


}
