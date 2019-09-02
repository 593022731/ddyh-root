package com.ddyh.order.service.services.handler;

import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.OrderHandler;
import org.springframework.stereotype.Service;

/**
 * 返利执行器
 * @author: weihui
 * @Date: 2019/8/27 11:24
 */
@Service
public class RebateHandler implements OrderHandler {
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(OrderContext context) {
        //TODO 直接调用返利系统返利方法，返利系统进行策略模式自行处理各种订单类型的返利处理
        return true;
    }
}
