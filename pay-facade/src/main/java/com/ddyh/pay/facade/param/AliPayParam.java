package com.ddyh.pay.facade.param;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * z 支付宝支付请求参数
 *
 * @author: weihui
 * @Date: 2019/8/19 15:06
 */
public class AliPayParam extends RequestParam implements Serializable {
    /**
     * 总金额，单位元
     */
    private BigDecimal totalFee;
    /**
     * 商品名称
     */
    private String subject;

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
}
