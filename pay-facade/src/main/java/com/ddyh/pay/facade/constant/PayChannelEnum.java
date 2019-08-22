package com.ddyh.pay.facade.constant;

/**
 * 交易渠道
 */
public enum PayChannelEnum {

    ALI_APP_PAY("ali_app_pay", "支付宝APP支付渠道"),
    WX_APP_PAY("wx_app_pay", "微信APP支付渠道"),
    WX_H5_PAY("wx_h5_pay", "微信H5支付渠道"),
    ALI_REFUND("ali_refund", "支付宝退款渠道"),
    WECHAT_REFUND("wechat_refund", "微信退款渠道");

    private String code;

    private String desc;

    PayChannelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

}