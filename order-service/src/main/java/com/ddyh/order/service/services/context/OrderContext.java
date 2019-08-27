package com.ddyh.order.service.services.context;

/**
 * 订单上下文对象
 * @author: weihui
 * @Date: 2019/8/26 16:15
 */
public class OrderContext {

    /** 订单类型*/
    private Integer orderType;

    /**uid*/
    private Long uid;

    private Long orderID;

    private String orderNum;

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
