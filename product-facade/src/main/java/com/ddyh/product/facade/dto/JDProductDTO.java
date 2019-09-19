package com.ddyh.product.facade.dto;

import java.io.Serializable;

/**
 * 从京东获取的商品DTO
 *
 * @author: weihui
 * @Date: 2019/6/10 11:06
 */
public class JDProductDTO implements Serializable {

    private Long sku;
    private String name;
    private String brandName;
    //京东价格
    private Double jdPrice;
    // 零售价
    private Double price;
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

    private boolean canSale;

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
}
