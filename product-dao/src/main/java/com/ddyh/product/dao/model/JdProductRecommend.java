package com.ddyh.product.dao.model;

import java.util.Date;

public class JdProductRecommend {
    private Long sku;

    private String name;

    private String brandName;

    /**
     *  京东价格(普通用户购买价格)
     */
    private Double jdPrice;

    /**
     *  目前数据库存储的等于会员价格（京东接口的零售价格）
     */
    private Double price;

    /**
     *  是否可用 0:下架 1:上架
     */
    private Integer state;

    /**
     * 京东状态是否可用 0:下架 1:上架
     */
    private Integer jdState;

    private Integer cat0;

    private Integer cat1;

    private Integer cat2;

    /**
     *  利润
     */
    private Double profit;

    private String imgPath;

    /**
     *  东东价：东东上会员价(字段名member_price)；
     */
    private Double memberPrice;

    /**
     *  采购价：跟渠道结算的成本价（又叫供货价 (字段名purchase_price)
     */
    private Double purchasePrice;

    /**
     * 是否精选推荐(0:否，1:是)
     */
    private Integer recommendType;

    /**
     * 是否超级尖货(0:否，1:是)
     */
    private Integer pointedCargo;

    /**
     * 是否体验卡邀请( 0: 否, 1: 是)
     */
    private Integer experienceCardInvite;

    /**
     * 是否9折
     */
    private Integer isHeightDiscount;

    /**
     * 自定义排序规则
     */
    private Integer customSort;

    /**
     * 采购折扣率
     */
    private Double discountRate;

    /**
     * 会员折扣率
     */
    private Double memDiscountRate;

    private Date requestTime;
    private Date updateTime;

    public Long getSku() {
        return sku;
    }

    public void setSku(Long sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Double getJdPrice() {
        return jdPrice;
    }

    public void setJdPrice(Double jdPrice) {
        this.jdPrice = jdPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getCat0() {
        return cat0;
    }

    public void setCat0(Integer cat0) {
        this.cat0 = cat0;
    }

    public Integer getCat1() {
        return cat1;
    }

    public void setCat1(Integer cat1) {
        this.cat1 = cat1;
    }

    public Integer getCat2() {
        return cat2;
    }

    public void setCat2(Integer cat2) {
        this.cat2 = cat2;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Double getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(Double memberPrice) {
        this.memberPrice = memberPrice;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getRecommendType() {
        return recommendType;
    }

    public void setRecommendType(Integer recommendType) {
        this.recommendType = recommendType;
    }

    public Integer getPointedCargo() {
        return pointedCargo;
    }

    public void setPointedCargo(Integer pointedCargo) {
        this.pointedCargo = pointedCargo;
    }

    public Integer getIsHeightDiscount() {
        return isHeightDiscount;
    }

    public void setIsHeightDiscount(Integer isHeightDiscount) {
        this.isHeightDiscount = isHeightDiscount;
    }

    public Integer getCustomSort() {
        return customSort;
    }

    public void setCustomSort(Integer customSort) {
        this.customSort = customSort;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public Double getMemDiscountRate() {
        return memDiscountRate;
    }

    public void setMemDiscountRate(Double memDiscountRate) {
        this.memDiscountRate = memDiscountRate;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getJdState() {
        return jdState;
    }

    public void setJdState(Integer jdState) {
        this.jdState = jdState;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Integer getExperienceCardInvite() {
        return experienceCardInvite;
    }

    public void setExperienceCardInvite(Integer experienceCardInvite) {
        this.experienceCardInvite = experienceCardInvite;
    }
}