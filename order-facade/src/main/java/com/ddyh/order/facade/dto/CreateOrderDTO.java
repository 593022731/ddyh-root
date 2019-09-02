package com.ddyh.order.facade.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 下单DTO
 */
public class CreateOrderDTO implements Serializable {

    private String orderNum;        // 订单编号

    private Date orderTime;         // 下单日期

    private BigDecimal orderPrice;     // 订单总金额


    /**
     * 无库存的sku
     */
    private List<Long> noStockSku;

    /**
     * 无效的(下架的)sku
     */
    private List<Long> invalidSku;

    /**
     * 不可售（京东）sku
     */
    private List<Long> unSaleSku;

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public List<Long> getNoStockSku() {
        return noStockSku;
    }

    public void setNoStockSku(List<Long> noStockSku) {
        this.noStockSku = noStockSku;
    }

    public List<Long> getInvalidSku() {
        return invalidSku;
    }

    public void setInvalidSku(List<Long> invalidSku) {
        this.invalidSku = invalidSku;
    }

    public List<Long> getUnSaleSku() {
        return unSaleSku;
    }

    public void setUnSaleSku(List<Long> unSaleSku) {
        this.unSaleSku = unSaleSku;
    }
}
