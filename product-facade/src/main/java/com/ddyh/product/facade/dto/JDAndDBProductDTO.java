package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 京东和商品库 共同返回的DTO
 *
 * @author: weihui
 * @Date: 2019/6/10 11:06
 */
public class JDAndDBProductDTO implements Serializable {

    private Long sku;
    private String name;
    private String brandName;
    //京东价格
    private Double jdPrice;

    // 状态
    private Integer state;
    // 一级分类
    private Integer cat0;
    private Integer cat1;
    private Integer cat2;
    //京东图片地址
    private String imagePath;
    //商品分类（一二三级）
    private String category;
    //视频地址
    private String videoPath;
    //重量
    private String weight;
    //产地
    private String productArea;
    //条形号
    private String upc;
    //销售单位
    private String saleUnit;
    //商品清单
    private String wareQD;
    //pc端商品简介
    private String introduction;
    //app端商品简介
    private String appintroduce;
    //商品参数
    private String param;
    //规格参数
    private String propCode;
    //服务
    private String service;
    //readme
    private String wReadMe;
    //售后
    private String shouhou;
    //商品详情
    private String wdis;
    //js内容
    private String jsContent;
    //CSS内容
    private String cssContent;
    //html内容
    private String htmlContent;
    // 采购价格
    private Double sonnhePrice;

    ///////////////以下是自己商品库的字段///////////////////
    //  目前数据库存储的等于会员价格（京东接口的零售价格）
    private Double price;
    //图片地址
    private String imgPath;
    // 利润
    private Double profit;
    // 供货价格
    private Double purchasePrice;
    // 会员价格
    private Double memberPrice;

    // 精选推荐(1:是，0：否)
    private Integer recommendType;

    private Integer pointedCargo;
    // 是否9折
    private Integer isHeightDiscount;
    // 自定义排序规则
    private Integer customSort;
    // 折扣率
    private Double discountRate;

    private boolean canSale;

    //基数 = 成交价-供货价-（成交价-供货价）*13%-成交价*0.6%
    //金额 = 基数 * 50%
    /**
     * 会员分享赚
     */
    private BigDecimal shareProfitPrice;

    public BigDecimal getShareProfitPrice() {
        BigDecimal memberPriceDecimal = new BigDecimal(memberPrice);
        BigDecimal purchasePriceDecimal = new BigDecimal(purchasePrice);
        BigDecimal subVal = memberPriceDecimal.subtract(purchasePriceDecimal);
        BigDecimal memberProfit = subVal.subtract(subVal.multiply(new BigDecimal(0.13))).subtract(memberPriceDecimal.multiply(new BigDecimal(0.006)));
        BigDecimal bigDecimal = memberProfit.multiply(new BigDecimal(0.5)).setScale(2, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : bigDecimal;
    }

    public JDAndDBProductDTO(JDProductDTO jd, ProductDTO db) {
        // db
        this.sku = db.getSku();

        this.name = jd.getName();
        this.brandName = jd.getBrandName();
        //采购价格
//        this.sonnhePrice = jd.getPrice();
        this.sonnhePrice = db.getPurchasePrice();
        this.purchasePrice = db.getPurchasePrice();
        // db
        this.price = db.getPrice();
        this.jdPrice = db.getJdPrice();
        this.state = db.getState();
        this.cat0 = db.getCat0();

        this.cat1 = jd.getCat1();
        this.cat2 = jd.getCat2();
        // db
        this.imgPath = db.getImgPath();
        this.memberPrice = db.getMemberPrice();
        this.recommendType = db.getRecommendType();
        this.pointedCargo = db.getPointedCargo();

        this.weight = jd.getWeight();
        this.productArea = jd.getProductArea();
        this.upc = jd.getUpc();
        this.saleUnit = jd.getSaleUnit();
        this.wareQD = jd.getWareQD();
        this.introduction = jd.getIntroduction();
        this.appintroduce = jd.getAppintroduce();
        this.param = jd.getParam();
        this.propCode = jd.getPropCode();
        this.service = jd.getService();
        this.wReadMe = jd.getwReadMe();
        this.shouhou = jd.getShouhou();
        this.wdis = jd.getWdis();
        this.jsContent = jd.getJsContent();
        this.cssContent = jd.getCssContent();
        this.htmlContent = jd.getHtmlContent();
        this.canSale = jd.isCanSale();
    }

    public boolean isCanSale() {
        return canSale;
    }

    public void setCanSale(boolean canSale) {
        this.canSale = canSale;
    }

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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getProductArea() {
        return productArea;
    }

    public void setProductArea(String productArea) {
        this.productArea = productArea;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public String getSaleUnit() {
        return saleUnit;
    }

    public void setSaleUnit(String saleUnit) {
        this.saleUnit = saleUnit;
    }

    public String getWareQD() {
        return wareQD;
    }

    public void setWareQD(String wareQD) {
        this.wareQD = wareQD;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAppintroduce() {
        return appintroduce;
    }

    public void setAppintroduce(String appintroduce) {
        this.appintroduce = appintroduce;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getPropCode() {
        return propCode;
    }

    public void setPropCode(String propCode) {
        this.propCode = propCode;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getwReadMe() {
        return wReadMe;
    }

    public void setwReadMe(String wReadMe) {
        this.wReadMe = wReadMe;
    }

    public String getShouhou() {
        return shouhou;
    }

    public void setShouhou(String shouhou) {
        this.shouhou = shouhou;
    }

    public String getWdis() {
        return wdis;
    }

    public void setWdis(String wdis) {
        this.wdis = wdis;
    }

    public String getJsContent() {
        return jsContent;
    }

    public void setJsContent(String jsContent) {
        this.jsContent = jsContent;
    }

    public String getCssContent() {
        return cssContent;
    }

    public void setCssContent(String cssContent) {
        this.cssContent = cssContent;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Double getSonnhePrice() {
        return sonnhePrice;
    }

    public void setSonnhePrice(Double sonnhePrice) {
        this.sonnhePrice = sonnhePrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(Double memberPrice) {
        this.memberPrice = memberPrice;
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
}
