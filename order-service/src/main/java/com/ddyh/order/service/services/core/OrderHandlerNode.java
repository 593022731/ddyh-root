package com.ddyh.order.service.services.core;

import com.ddyh.order.service.services.context.OrderContext;

/**
 * Handler Node Chain
 * @author: weihui
 * @Date: 2019/8/27 11:05
 */
public class OrderHandlerNode {

    private OrderHandler handler;

    private OrderHandlerNode next = null;

    public OrderHandlerNode(){}

    public OrderHandlerNode(OrderHandler handler){
        this.handler = handler;
    }

    public void execute(OrderContext context) {
        boolean success = false;
        if (handler.isAsync()) {
            //TODO 采用异步线程Future去执行
        }else{
            success = handler.handle(context);
        }
        if (next != null) {
            if (success) {
                next.execute(context);
            }
        }
    }

    public OrderHandlerNode getNext() {
        return next;
    }

    public void setNext(OrderHandlerNode next) {
        this.next = next;
    }
}
