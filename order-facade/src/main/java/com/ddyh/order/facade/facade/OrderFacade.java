package com.ddyh.order.facade.facade;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.param.OrderParam;

/**
 * @author: weihui
 * @Date: 2019/8/26 15:22
 */
public interface OrderFacade {

    /**
     * 创建订单
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    Result createOrder(OrderParam param) throws BusinessException;

    /**
     * 支付回调
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    Result callBackOrder(OrderParam param) throws BusinessException;
}
