package com.ddyh.pay.facade.param;

import java.io.Serializable;

/**
 * z 微信支付请求参数
 *
 * @author: weihui
 * @Date: 2019/8/19 15:06
 */
public class WXPayParam extends RequestParam implements Serializable {
    /**
     * 总金额，单位分
     */
    private Integer totalFee;

    /**
     * 终端IP
     */
    private String spbillCreateIp;
    /** 微信openID(微信h5支付必填) */
    private String openId;

    /** 商品描述 */
    private String body;

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getSpbillCreateIp() {
        return spbillCreateIp;
    }

    public void setSpbillCreateIp(String spbillCreateIp) {
        this.spbillCreateIp = spbillCreateIp;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
