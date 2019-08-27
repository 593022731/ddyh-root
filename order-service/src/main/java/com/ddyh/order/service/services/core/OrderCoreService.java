package com.ddyh.order.service.services.core;

import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.param.OrderParam;

/**
 * @author: weihui
 * @Date: 2019/8/26 16:02
 */
public interface OrderCoreService {

    /**
     * 创建订单
     * @param param
     * @return
     */
    Result createOrder(OrderParam param);

    /**
     * 支付回调
     * @param param
     * @return
     */
    Result callBackOrder(OrderParam param);


}
