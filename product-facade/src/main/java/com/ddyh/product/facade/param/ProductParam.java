package com.ddyh.product.facade.param;

import com.ddyh.commons.param.PageParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 商品参数对象
 *
 * @author: weihui
 * @Date: 2019/6/10 11:14
 */
public class ProductParam extends PageParam {
    private static final String DEFAULT_SORT = "update_time desc";

    private Long sku;
    private String name;
    private String brandName;
    // 状态
    private Integer state;
    // 一级分类
    private Integer cat0;
    private Integer cat1;
    private Integer cat2;
    // 开始会员价格
    private Double memberPriceBegin;
    // 结束会员价格
    private Double memberPriceEnd;
    // 开始折扣率
    private Double discountRateBegin;
    // 结束折扣率
    private Double discountRateEnd;
    // 利润率
    private Double profitRate;
    // 关键字
    private String keyword;
    // 批量sku
    private List<Long> skus;
    // 批量一级分类ID
    private List<Integer> cat0s;
    // 批量二级分类ID
    private List<Integer> cat1s;
    // 批量三级分类ID
    private List<Integer> cat2s;

    private String orderBy;

    private Integer recommendType;
    private Integer pointedCargo;
    private Integer experienceCardInvite;
    private Integer customSort;

    /**
     * 频道标签
     */
    private String productLabel;

    private Integer jdState;

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

    public Double getDiscountRateBegin() {
        return discountRateBegin;
    }

    public void setDiscountRateBegin(Double discountRateBegin) {
        this.discountRateBegin = discountRateBegin;
    }

    public Double getDiscountRateEnd() {
        return discountRateEnd;
    }

    public void setDiscountRateEnd(Double discountRateEnd) {
        this.discountRateEnd = discountRateEnd;
    }

    public Double getMemberPriceBegin() {
        return memberPriceBegin;
    }

    public void setMemberPriceBegin(Double memberPriceBegin) {
        this.memberPriceBegin = memberPriceBegin;
    }

    public Double getMemberPriceEnd() {
        return memberPriceEnd;
    }

    public void setMemberPriceEnd(Double memberPriceEnd) {
        this.memberPriceEnd = memberPriceEnd;
    }

    public String getOrderBy() {
        if(StringUtils.isEmpty(orderBy)) {
            return DEFAULT_SORT;
        }
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getProductLabel() {
        return productLabel;
    }

    public void setProductLabel(String productLabel) {
        this.productLabel = productLabel;
    }


    public Integer getRecommendType() {
        if (ProductLabelEnum.RECOMMEND_TYPE.getName().equals(productLabel)) {
            return 1;
        }
        return recommendType;
    }

    public void setRecommendType(Integer recommendType) {
        this.recommendType = recommendType;
    }

    public Integer getPointedCargo() {
        if (ProductLabelEnum.POINTED_CARGO.getName().equals(productLabel)) {
            return 1;
        }
        return pointedCargo;
    }

    public void setPointedCargo(Integer pointedCargo) {
        this.pointedCargo = pointedCargo;
    }

    public Integer getJdState() {
        return jdState;
    }

    public void setJdState(Integer jdState) {
        this.jdState = jdState;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Long> getSkus() {
        return skus;
    }

    public void setSkus(List<Long> skus) {
        this.skus = skus;
    }

    public List<Integer> getCat0s() {
        return cat0s;
    }

    public void setCat0s(List<Integer> cat0s) {
        this.cat0s = cat0s;
    }

    public Double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(Double profitRate) {
        this.profitRate = profitRate;
    }

    public List<Integer> getCat1s() {
        return cat1s;
    }

    public void setCat1s(List<Integer> cat1s) {
        this.cat1s = cat1s;
    }

    public List<Integer> getCat2s() {
        return cat2s;
    }

    public void setCat2s(List<Integer> cat2s) {
        this.cat2s = cat2s;
    }

    public Integer getExperienceCardInvite() {
        if (ProductLabelEnum.EXPERIENCE_CARD_INVITE.getName().equals(productLabel)) {
            return 1;
        }
        return experienceCardInvite;
    }

    public void setExperienceCardInvite(Integer experienceCardInvite) {
        this.experienceCardInvite = experienceCardInvite;
    }

    public Integer getCustomSort() {
        return customSort;
    }

    public void setCustomSort(Integer customSort) {
        this.customSort = customSort;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
