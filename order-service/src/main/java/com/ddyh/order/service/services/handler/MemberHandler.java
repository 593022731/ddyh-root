package com.ddyh.order.service.services.handler;

import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.OrderHandler;
import org.springframework.stereotype.Service;

/**
 * 会员执行器
 * @author: weihui
 * @Date: 2019/8/27 11:24
 */
@Service
public class MemberHandler implements OrderHandler {
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(OrderContext context) {
        //TODO 会员升级处理
        return true;
    }
}
