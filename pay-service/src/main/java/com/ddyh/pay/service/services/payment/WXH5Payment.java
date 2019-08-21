package com.ddyh.pay.service.services.payment;


import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.facade.param.WXPayParam;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.context.WXPaymentContext;
import com.ddyh.pay.service.services.validator.Validator;
import com.ddyh.pay.service.services.validator.WXH5PaymentValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 微信H5支付
 *
 * @author: weihui
 * @Date: 2019/8/19 16:17
 */
@Service
public class WXH5Payment extends WXPayment {

    /**
     * 应用id
     */
    @Value("${wx.h5.pay.appId}")
    protected String appId;
    /**
     * 商户号
     */
    @Value("${wx.h5.pay.mchId}")
    protected String mchId;
    /**
     * 商户密钥key
     */
    @Value("${wx.h5.pay.mchKey}")
    protected String mchKey;

    @Resource
    private WXH5PaymentValidator wxH5PaymentValidator;

    @Override
    public void prepare(PaymentContext context) {
        super.appId = this.appId;
        super.mchId = this.mchId;
        super.mchKey = this.mchKey;
        super.prepare(context);
    }

    @Override
    public PaymentContext createContext(RequestParam param) {
        WXPayParam wxParam = (WXPayParam) param;
        WXPaymentContext wxPaymentContext = (WXPaymentContext) super.createContext(param);
        wxPaymentContext.setOpenId(wxParam.getOpenId());
        wxPaymentContext.setTradeType("JSAPI");
        wxPaymentContext.setSignType("MD5");
        return wxPaymentContext;
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_H5_PAY.getCode();
    }

    @Override
    public Validator getValidator() {
        return wxH5PaymentValidator;
    }

    @Override
    public void afterProcess(PaymentContext context, Result result) {
        //TODO 生成交易日志表，交易中
    }

}
