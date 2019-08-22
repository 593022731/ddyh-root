package com.ddyh.pay.facade.constant;

/**
 * 交易类型
 */
public enum TradeTypeEnum {

    PAY(1, "支付"),
    REFUND(2, "退款"),
    EXCHANG(3, "打款");

    private Integer code;

    private String desc;

    TradeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
}