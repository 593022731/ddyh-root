package com.ddyh.product.facade.dto;

import com.ddyh.product.facade.constant.ProductConstant;

import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/8/6 15:56
 * @descript: 商品是否可售
 */
public class JDProductCanSaleDTO implements Serializable {

    private Long skuId;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 是否可售，1：是，0：否
     */
    private Integer saleState;
    /**
     * 是否可开增票，1：支持，0：不支持
     */
    private Integer isCanVAT;

    private boolean canSale;

    public boolean isCanSale() {
        return ProductConstant.STATE_ON_SALE.equals(saleState);
    }

    public void setCanSale(boolean canSale) {
        this.canSale = canSale;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSaleState() {
        return saleState;
    }

    public void setSaleState(Integer saleState) {
        this.saleState = saleState;
    }

    public Integer getIsCanVAT() {
        return isCanVAT;
    }

    public void setIsCanVAT(Integer isCanVAT) {
        this.isCanVAT = isCanVAT;
    }
}
