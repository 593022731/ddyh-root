package com.ddyh.order.service.services.validator;

import com.ddyh.order.service.services.context.OrderContext;

/**
 * 数据验证接口类
 * @author
 */
public interface Validator {
    /**
     * 数据验证
     * @param context
     */
    void validate(OrderContext context);
}