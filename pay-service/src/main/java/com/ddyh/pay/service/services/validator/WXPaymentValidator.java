package com.ddyh.pay.service.services.validator;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.context.WXPaymentContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("wxPaymentValidator")
public class WXPaymentValidator extends BaseValidator {

    @Override
    public void specialValidate(PaymentContext context) {
        WXPaymentContext wxContext = (WXPaymentContext)context;
        if (!wxContext.getPayChannel().equals(PayChannelEnum.WECHAT_PAY.getCode())) {
            throw new BusinessException("支付渠道参数异常");
        }
        if(wxContext.getTotalFee() == null){
            throw new BusinessException("订单金额不能为空");
        }
        if(StringUtils.isBlank(wxContext.getSpbillCreateIp())){
            throw new BusinessException("IP不能为空");
        }
        //TODO 支付订单是否已存在,防止金额篡改
    }
}
