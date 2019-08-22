package com.ddyh.pay.facade.dto;

import java.io.Serializable;

/**
 * H5调用微信支付参数
 */
public class WXH5PayDTO implements Serializable {
    /**
     * 应用ID
     */
    private String appId;

    /**
     * 订单详情扩展字符串 package=prepayId
     * 因为package是关键字，不能作为变量，用prepayId代替
     */
    private String prepayId;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 随机字符串
     */
    private String nonceStr;

    /**
     * 时间戳
     */
    private String timeStamp;

    /**
     * 签名
     */
    private String paySign;

    public WXH5PayDTO(){}

    public WXH5PayDTO(String appId, String prepayId, String signType, String nonceStr, String timeStamp, String paySign) {
        this.appId = appId;
        this.prepayId = prepayId;
        this.signType = signType;
        this.nonceStr = nonceStr;
        this.timeStamp = timeStamp;
        this.paySign = paySign;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPaySign() {
        return paySign;
    }

    public void setPaySign(String paySign) {
        this.paySign = paySign;
    }
}
