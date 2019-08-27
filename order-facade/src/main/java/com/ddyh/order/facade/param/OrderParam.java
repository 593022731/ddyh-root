package com.ddyh.order.facade.param;

import java.io.Serializable;

/**
 * @author: weihui
 * @Date: 2019/8/26 16:03
 */
public class OrderParam implements Serializable {

    /** 订单类型*/
    private Integer orderType;

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
}
