package com.ddyh.order.service.services.validator;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.order.service.services.context.OrderContext;

public abstract class BaseValidator implements Validator {

    @Override
    public void validate(OrderContext context) {
        if (context.getOrderType() == null) {
            throw new BusinessException("订单类型不能为空");
        }
        if (context.getUid() == null) {
            throw new BusinessException("UID不能为空");
        }
        //特殊校验
        specialValidate(context);

    }

    public abstract void specialValidate(OrderContext context);
}