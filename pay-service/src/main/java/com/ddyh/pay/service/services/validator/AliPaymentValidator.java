package com.ddyh.pay.service.services.validator;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.service.services.context.AliPaymentContext;
import com.ddyh.pay.service.services.context.PaymentContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("aliPaymentValidator")
public class AliPaymentValidator extends BaseValidator {

    @Override
    public void specialValidate(PaymentContext context) {
        AliPaymentContext aliContext = (AliPaymentContext)context;
        if (!aliContext.getPayChannel().equals(PayChannelEnum.ALI_PAY.getCode())) {
            throw new BusinessException("支付渠道参数异常");
        }
        if(aliContext.getTotalFee() == null){
            throw new BusinessException("订单金额不能为空");
        }
        if (StringUtils.isBlank(aliContext.getSubject())) {
            throw new BusinessException("商品名称不能为空");
        }
        //TODO 支付订单是否已存在,防止金额篡改
    }
}
