package com.ddyh.pay.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.facade.PayFacade;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.service.services.core.BasePayCoreService;
import com.ddyh.pay.service.services.core.PayCoreService;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:07
 */
@SuppressWarnings("all")
@Service(interfaceClass = PayFacade.class)
public class PayFacadeImpl implements PayFacade {

    @Override
    public Result getRequest(RequestParam param)  throws BusinessException {
        if(param == null || StringUtils.isBlank(param.getPayChannel())){
            throw new BusinessException("参数为空");
        }
        PayCoreService service = BasePayCoreService.paymentMap.get(param.getPayChannel());
        if(service == null){
            throw new BusinessException("参数不合法");
        }
        return service.getRequest(param);
    }

    @Override
    public Result callback(CallBackParam param)  throws BusinessException{
        if(param == null || StringUtils.isBlank(param.getPayChannel())){
            throw new BusinessException("参数为空");
        }
        PayCoreService service = BasePayCoreService.paymentMap.get(param.getPayChannel());
        if(service == null){
            throw new BusinessException("参数不合法");
        }
        return service.callback(param);
    }
}
