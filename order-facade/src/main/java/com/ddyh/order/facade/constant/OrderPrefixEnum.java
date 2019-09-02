package com.ddyh.order.facade.constant;

public enum OrderPrefixEnum {
    GIFT("11"),       // 大礼包
    JD_GOODS("21"),     // 京东商品
    PRE_BUY("31"),       // 团购
    JD_GIFT("51"),       // 京东大礼盒
    EXP_CARD("51");       // 体验卡

    private String code;

    OrderPrefixEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
