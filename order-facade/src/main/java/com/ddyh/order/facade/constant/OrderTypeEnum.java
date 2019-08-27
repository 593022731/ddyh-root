package com.ddyh.order.facade.constant;

public enum OrderTypeEnum {
    GIFT(1),       // 大礼包
    JD_GOODS(2),     // 京东商品
    PRE_BUY(3),       // 团购
    JD_GIFT(3);       // 京东大礼盒

    private int type;

    OrderTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
