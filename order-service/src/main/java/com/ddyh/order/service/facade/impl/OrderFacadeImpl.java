package com.ddyh.order.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.facade.OrderFacade;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.service.services.core.BaseOrderCoreService;
import com.ddyh.order.service.services.core.OrderCoreService;

/**
 * @author: weihui
 * @Date: 2019/8/26 15:27
 */
@SuppressWarnings("all")
@Service(interfaceClass = OrderFacade.class)
public class OrderFacadeImpl implements OrderFacade {

    @Override
    public Result createOrder(OrderParam param) throws BusinessException {
        if (param.getOrderType() == null) {
            throw new BusinessException("参数为空");
        }
        OrderCoreService orderCoreService = BaseOrderCoreService.orderMap.get(param.getOrderType());
        if (orderCoreService == null) {
            throw new BusinessException("参数不合法");
        }
        return orderCoreService.createOrder(param);
    }

    @Override
    public Result callBackOrder(OrderParam param) throws BusinessException {
        if (param.getOrderType() == null) {
            throw new BusinessException("参数为空");
        }
        OrderCoreService orderCoreService = BaseOrderCoreService.orderMap.get(param.getOrderType());
        if (orderCoreService == null) {
            throw new BusinessException("参数不合法");
        }
        return orderCoreService.callBackOrder(param);
    }
}
