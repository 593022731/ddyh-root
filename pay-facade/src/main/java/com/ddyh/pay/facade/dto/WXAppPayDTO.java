package com.ddyh.pay.facade.dto;

import java.io.Serializable;

/**
 * APP调用微信支付参数
 */
public class WXAppPayDTO implements Serializable {

    /**
     * 应用ID
     */
    private String appid;

    /**
     * 商户号
     */
    private String partnerid;

    /**
     * 预支付交易会话ID
     */
    private String prepayid;

    /**
     * 扩展字段,package=signType
     * 因为package是关键字，不能作为变量，用signType代替
     */
    private String signType;

    /**
     * 随机字符串
     */
    private String noncestr;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 签名
     */
    private String sign;

    public WXAppPayDTO(){}

    public WXAppPayDTO(String appid, String partnerid, String prepayid, String signType, String noncestr, String timestamp, String sign) {
        this.appid = appid;
        this.partnerid = partnerid;
        this.prepayid = prepayid;
        this.signType = signType;
        this.noncestr = noncestr;
        this.timestamp = timestamp;
        this.sign = sign;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
