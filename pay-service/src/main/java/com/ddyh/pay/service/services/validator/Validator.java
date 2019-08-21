package com.ddyh.pay.service.services.validator;

import com.ddyh.pay.service.services.context.PaymentContext;

/**
 * 数据验证接口类
 * @author
 */
public interface Validator {
    /**
     * 数据验证
     * @param context
     */
    void validate(PaymentContext context);
}