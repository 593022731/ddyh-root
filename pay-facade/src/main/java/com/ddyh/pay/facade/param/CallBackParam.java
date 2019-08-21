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

    /** 支付/退款通知结果渠道*/
    private Map<String,String[]> resultMap;

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public Map<String, String[]> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, String[]> resultMap) {
        this.resultMap = resultMap;
    }
}
