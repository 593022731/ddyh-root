package com.ddyh.pay.facade.param;

import java.io.Serializable;

/**
 *z 父类请求参数
 * @author: weihui
 * @Date: 2019/8/19 15:06
 */

public class RequestParam implements Serializable {
    /** 商户订单号 */
    private String tradeNo;
    /** 支付/退款渠道*/
    private String payChannel;

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }
}
