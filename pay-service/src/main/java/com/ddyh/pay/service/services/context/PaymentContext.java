package com.ddyh.pay.service.services.context;

/**
 * 支付上下文
 * @author: weihui
 * @Date: 2019/8/19 16:27
 */
public class PaymentContext{
    /** 商户订单号*/
    private String outTradeNo;
    /** 支付渠道*/
    private String payChannel;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }
}
