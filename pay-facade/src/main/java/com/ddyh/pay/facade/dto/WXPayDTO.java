package com.ddyh.pay.facade.dto;

import java.io.Serializable;

/**
 * 调用微信支付参数
 */
public class WXPayDTO implements Serializable {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户号
     */
    private String partnerId;

    /**
     * 预支付交易会话ID
     * 当APP唤醒时，参数名不变prepayid=prepayid
     * 当H5唤醒时，package=prepayId
     */
    private String prepayId;

    /**
     * 当APP唤醒时，package=signType
     * 当H5唤醒时，参数名不变signType=signType
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
    private String sign;

    public WXPayDTO(){}

    public WXPayDTO(String appId, String partnerId, String prepayId, String signType, String nonceStr, String timeStamp, String sign) {
        this.appId = appId;
        this.partnerId = partnerId;
        this.prepayId = prepayId;
        this.signType = signType;
        this.nonceStr = nonceStr;
        this.timeStamp = timeStamp;
        this.sign = sign;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
