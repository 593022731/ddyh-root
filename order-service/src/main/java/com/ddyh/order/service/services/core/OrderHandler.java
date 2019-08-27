package com.ddyh.order.service.services.core;

import com.ddyh.order.service.services.context.OrderContext;

/**
 * @author: weihui
 * @Date: 2019/8/27 10:58
 */
public interface OrderHandler {
    /**
     * 是否采用异步方式执行
     *
     * @return
     */
    boolean isAsync();

    /**
     * 执行交易具体业务
     *
     * @param context
     * @return true则继续执行下一个Handler，否则结束Handler Chain的执行直接返回
     */
    boolean handle(OrderContext context);
}
