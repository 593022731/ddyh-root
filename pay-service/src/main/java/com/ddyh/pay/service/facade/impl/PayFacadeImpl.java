package com.ddyh.pay.service.facade.impl;

import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.facade.PayFacade;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.service.services.core.BasePayCoreService;
import org.apache.dubbo.config.annotation.Service;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:07
 */
@Service(loadbalance = "random",timeout = 50000,cluster = "failsafe")
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
