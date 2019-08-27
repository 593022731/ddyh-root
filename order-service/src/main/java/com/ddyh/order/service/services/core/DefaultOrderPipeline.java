package com.ddyh.order.service.services.core;

import com.ddyh.order.service.services.context.OrderContext;

/**
 * 默认管道对象
 *
 * @author: weihui
 * @Date: 2019/8/27 11:01
 */
@SuppressWarnings("all")
public class DefaultOrderPipeline implements OrderPipeline {

    private OrderHandlerNode head;

    private OrderHandlerNode tail;

    private OrderContext context;

    public DefaultOrderPipeline(OrderContext context) {
        this.context = context;
        head = new OrderHandlerNode();
        tail = head;
    }


    @Override
    public void addFirst(OrderHandler... handlers) {
        OrderHandlerNode pre = head.getNext();
        for (OrderHandler handler : handlers) {
            if (handler == null) {
                continue;
            }
            OrderHandlerNode node = new OrderHandlerNode(handler);
            node.setNext(pre);

            pre = node;
        }

        head.setNext(pre);
    }

    @Override
    public void addLast(OrderHandler... handlers) {
        OrderHandlerNode next = tail;
        for (OrderHandler handler : handlers) {
            if (handler == null) {
                continue;
            }

            OrderHandlerNode node = new OrderHandlerNode(handler);
            next.setNext(node);
            next = node;
        }

        tail = next;
    }

    @Override
    public void start() {
        head.getNext().execute(getContext());
    }

    @Override
    public void shutdown() {

    }

    @Override
    public <T extends OrderContext> T getContext() {
        return (T) context;
    }
}
