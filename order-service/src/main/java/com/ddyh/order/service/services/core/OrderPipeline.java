package com.ddyh.order.service.services.core;

/**
 * 订单管道
 * @author: weihui
 * @Date: 2019/8/27 10:57
 */
public interface OrderPipeline extends OrderInvoker {

    /**
     * 添加到头节点
     *
     * @param handlers
     */
    void addFirst(OrderHandler... handlers);

    /**
     * 添加到尾节点
     *
     * @param handlers
     */
    void addLast(OrderHandler... handlers);
}
