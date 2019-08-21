package com.ddyh.pay.service.services.validator;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.pay.service.services.context.PaymentContext;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseValidator implements Validator {

    @Override
    public void validate(PaymentContext context) {
        if (StringUtils.isBlank(context.getOutTradeNo())) {
            throw new BusinessException("商户订单号不能为空");
        }
        if (StringUtils.isBlank(context.getPayChannel())) {
            throw new BusinessException("支付渠道不能为空");
        }
        //特殊校验
        specialValidate(context);

    }

    public abstract void specialValidate(PaymentContext context);
}