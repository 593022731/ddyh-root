package com.ddyh.order.service.services.context;

import com.ddyh.order.facade.param.JDGoodsOrderParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * 京东商品订单上下文对象
 *
 * @author: weihui
 * @Date: 2019/8/26 16:16
 */
public class JDGoodsOrderContext extends OrderContext {

    private JDGoodsOrderParam param;


    private BigDecimal orderPrice;     // 订单总金额

    private BigDecimal orderNakedPrice;        // 订单裸价

    private BigDecimal orderFloorPrice;        // 订单底价总金额

    private BigDecimal orderFloorNakedPrice;        // 订单底价裸价

    private BigDecimal payPrice;            // 支付价格

    private Long jdOrderId;         // jd订单号

    private String hqOrderNum;      // 环球订单号

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

    public JDGoodsOrderParam getParam() {
        return param;
    }

    public void setParam(JDGoodsOrderParam param) {
        this.param = param;
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

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getOrderNakedPrice() {
        return orderNakedPrice;
    }

    public void setOrderNakedPrice(BigDecimal orderNakedPrice) {
        this.orderNakedPrice = orderNakedPrice;
    }

    public BigDecimal getOrderFloorPrice() {
        return orderFloorPrice;
    }

    public void setOrderFloorPrice(BigDecimal orderFloorPrice) {
        this.orderFloorPrice = orderFloorPrice;
    }

    public BigDecimal getOrderFloorNakedPrice() {
        return orderFloorNakedPrice;
    }

    public void setOrderFloorNakedPrice(BigDecimal orderFloorNakedPrice) {
        this.orderFloorNakedPrice = orderFloorNakedPrice;
    }

    public BigDecimal getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }

    public Long getJdOrderId() {
        return jdOrderId;
    }

    public void setJdOrderId(Long jdOrderId) {
        this.jdOrderId = jdOrderId;
    }

    public String getHqOrderNum() {
        return hqOrderNum;
    }

    public void setHqOrderNum(String hqOrderNum) {
        this.hqOrderNum = hqOrderNum;
    }
}
