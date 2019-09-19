package com.ddyh.product.facade.param;

import java.io.Serializable;

public class JDGiftProductParam implements Serializable {
    private Integer id;

    private String channelId;

    /**
     * 大礼包类型，sku=主键ID
     */
    private String skus;

    private String giftTitle;

    private Double giftPrice;

    private String giftImgpath;

    private Double originalPrice;

    private String sellingPoint;

    private String purLimitId;

    private String purLimitCity;

    private Integer giftType;

    private Integer state;

    private Integer isPub;

    private Integer isPri;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getSkus() {
        return skus;
    }

    public void setSkus(String skus) {
        this.skus = skus;
    }

    public String getGiftTitle() {
        return giftTitle;
    }

    public void setGiftTitle(String giftTitle) {
        this.giftTitle = giftTitle;
    }

    public Double getGiftPrice() {
        return giftPrice;
    }

    public void setGiftPrice(Double giftPrice) {
        this.giftPrice = giftPrice;
    }

    public String getGiftImgpath() {
        return giftImgpath;
    }

    public void setGiftImgpath(String giftImgpath) {
        this.giftImgpath = giftImgpath;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getSellingPoint() {
        return sellingPoint;
    }

    public void setSellingPoint(String sellingPoint) {
        this.sellingPoint = sellingPoint;
    }

    public String getPurLimitId() {
        return purLimitId;
    }

    public void setPurLimitId(String purLimitId) {
        this.purLimitId = purLimitId;
    }

    public String getPurLimitCity() {
        return purLimitCity;
    }

    public void setPurLimitCity(String purLimitCity) {
        this.purLimitCity = purLimitCity;
    }

    public Integer getGiftType() {
        return giftType;
    }

    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getIsPub() {
        return isPub;
    }

    public void setIsPub(Integer isPub) {
        this.isPub = isPub;
    }

    public Integer getIsPri() {
        return isPri;
    }

    public void setIsPri(Integer isPri) {
        this.isPri = isPri;
    }
}