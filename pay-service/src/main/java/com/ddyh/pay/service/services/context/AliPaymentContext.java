package com.ddyh.pay.service.services.context;

import com.alipay.api.request.AlipayTradeAppPayRequest;

import java.math.BigDecimal;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:27
 */
public class AliPaymentContext extends PaymentContext{
    /** 总金额，单位元*/
    private BigDecimal totalFee;
    /** 商品名称*/
    private String subject;
    /** 支付宝处理参数*/
    private AlipayTradeAppPayRequest request;

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public AlipayTradeAppPayRequest getRequest() {
        return request;
    }

    public void setRequest(AlipayTradeAppPayRequest request) {
        this.request = request;
    }
}