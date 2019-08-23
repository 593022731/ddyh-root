package com.ddyh.pay.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.facade.PayFacade;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.service.services.core.BasePayCoreService;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:07
 */
@Service(interfaceClass = PayFacade.class)
public class PayFacadeImpl implements PayFacade {

    @Override
    public Result getRequest(RequestParam param) {
        return BasePayCoreService.paymentMap.get(param.getPayChannel()).getRequest(param);
    }

    @Override
    public Result callback(CallBackParam param) {
        return BasePayCoreService.paymentMap.get(param.getPayChannel()).callback(param);
    }
}
