package com.ddyh.pay.service.services.context;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:27
 */
public class WXPaymentContext extends PaymentContext{
    /** 总金额，单位分*/
    private Integer totalFee;

    /** 终端IP */
    private String spbillCreateIp;
    /** 拼接的xml格式数据，用于微信统一下单的参数 */
    private String xml;
    /** 签名 */
    private String sign;
    /** 微信openID(微信h5支付必填) */
    private String openId;
    /** 交易类型 */
    private String tradeType;
    /** 签名类型 */
    private String signType;

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

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }
}