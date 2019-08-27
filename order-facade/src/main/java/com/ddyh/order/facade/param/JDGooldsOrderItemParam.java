package com.ddyh.order.facade.param;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 京东商品订单项参数
 * @author: weihui
 * @Date: 2019/8/26 16:45
 */
public class JDGooldsOrderItemParam implements Serializable {

    private Long skuId;     // 货品编号

    private Integer num;        // 产品数量

    /**
     * 如果是会员=member_price
     * 普通用户=jd_price
     */
    private BigDecimal salePrice;      // 销售价格

    /**
     * 采购价格purchase_price
     */
    private BigDecimal floorPrice;         // 底价，即采购价格

    /**
     * 京东零售价jd_price
     */
    private BigDecimal platformPrice;     // 平台价格，即非会员价格

    /**
     * 会员价member_price
     */
    private BigDecimal memberPrice;     // 会员价

    private String productName;     // 产品名称

    private String productPicture;      // 产品图片地址

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getFloorPrice() {
        return floorPrice;
    }

    public void setFloorPrice(BigDecimal floorPrice) {
        this.floorPrice = floorPrice;
    }

    public BigDecimal getPlatformPrice() {
        return platformPrice;
    }

    public void setPlatformPrice(BigDecimal platformPrice) {
        this.platformPrice = platformPrice;
    }

    public BigDecimal getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(BigDecimal memberPrice) {
        this.memberPrice = memberPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductPicture() {
        return productPicture;
    }

    public void setProductPicture(String productPicture) {
        this.productPicture = productPicture;
    }
}
