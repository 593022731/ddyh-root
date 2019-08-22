package com.ddyh.pay.facade.constant;

/**
 * 交易状态
 */
public enum TradeStatusEnum {

    FAIL((short)-1, "交易失败"),
    COMMIT((short)1, "交易中"),
    FINISH((short)2, "交易完成");

    private Short code;

    private String desc;

    TradeStatusEnum(Short code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Short getCode() {
        return code;
    }

}