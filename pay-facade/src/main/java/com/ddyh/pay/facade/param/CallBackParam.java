package com.ddyh.pay.facade.param;

import java.io.Serializable;
import java.util.Map;

/**
 * 回调参数
 * @author: weihui
 * @Date: 2019/8/20 15:24
 */
public class CallBackParam implements Serializable {
    /** 支付/退款渠道*/
    private String payChannel;

    /** 支付/退款通知结果渠道(支付宝回调参数)*/
    private Map<String,String[]> aliParam;

    /** 支付/退款通知结果渠道(微信回调参数,xml)*/
    private String wxParam;

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public Map<String, String[]> getAliParam() {
        return aliParam;
    }

    public void setAliParam(Map<String, String[]> aliParam) {
        this.aliParam = aliParam;
    }

    public String getWxParam() {
        return wxParam;
    }

    public void setWxParam(String wxParam) {
        this.wxParam = wxParam;
    }
}
