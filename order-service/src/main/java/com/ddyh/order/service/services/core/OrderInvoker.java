package com.ddyh.order.service.services.core;

import com.ddyh.order.service.services.context.OrderContext;

/**
 * 订单回调执行器
 * @author: weihui
 * @Date: 2019/8/27 10:51
 */
public interface OrderInvoker {

    /**
     * 启动流程
     *
     */
    void start();

    /**
     * 终止流程
     *
     */
    void shutdown();

    /**
     * 获取返回值
     *
     * @return
     */
    <T extends OrderContext> T getContext();
}
