package com.ddyh.product.facade.dto;

import java.io.Serializable;

/**
 * 库存状态
 */
public class JDStockStateDTO implements Serializable {

    //地域id
    private String areaId;

    //地域名称
    private String areaName;

    //skuId
    private Long skuId;

    //库存状态id
    private Integer stockStateId;

    //库存状态描述
    private String stockStateDesc;

    //库存数量
    private Integer remainNum;

    //商品价格
    private Double price;

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getStockStateId() {
        return stockStateId;
    }

    public void setStockStateId(Integer stockStateId) {
        this.stockStateId = stockStateId;
    }

    public String getStockStateDesc() {
        return stockStateDesc;
    }

    public void setStockStateDesc(String stockStateDesc) {
        this.stockStateDesc = stockStateDesc;
    }

    public Integer getRemainNum() {
        return remainNum;
    }

    public void setRemainNum(Integer remainNum) {
        this.remainNum = remainNum;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}