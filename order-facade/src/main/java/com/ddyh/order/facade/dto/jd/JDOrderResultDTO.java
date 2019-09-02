package com.ddyh.order.facade.dto.jd;

import java.io.Serializable;

/**
 *  下单返回结果
 */
public class JDOrderResultDTO implements Serializable {

    private String orderSn;

    private Double totalFee;

    private Integer totalProduct;

    private Integer userId;

    private Integer venderId;

    private String jdTradeNo;

    private Integer feight;

    private Double orderPrice;

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public Double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getTotalProduct() {
        return totalProduct;
    }

    public void setTotalProduct(Integer totalProduct) {
        this.totalProduct = totalProduct;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getVenderId() {
        return venderId;
    }

    public void setVenderId(Integer venderId) {
        this.venderId = venderId;
    }

    public String getJdTradeNo() {
        return jdTradeNo;
    }

    public void setJdTradeNo(String jdTradeNo) {
        this.jdTradeNo = jdTradeNo;
    }

    public Integer getFeight() {
        return feight;
    }

    public void setFeight(Integer feight) {
        this.feight = feight;
    }

    public Double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Double orderPrice) {
        this.orderPrice = orderPrice;
    }
}
