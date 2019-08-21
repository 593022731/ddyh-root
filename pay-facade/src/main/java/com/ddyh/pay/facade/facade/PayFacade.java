package com.ddyh.pay.facade.facade;


import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;

/**
 * 支付服务
 * @author: weihui
 * @Date: 2019/8/19 14:57
 */
public interface PayFacade {

    /**
     * 获取(支付/退款)订单信息
     * @param param
     * @return
     */
    Result getRequest(RequestParam param) throws BusinessException;

    /**
     * 回调处理
     * @param param
     * @return
     */
    Result callback(CallBackParam param) throws BusinessException;
}
