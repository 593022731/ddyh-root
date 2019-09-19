package com.ddyh.product.facade.param;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: weihui
 * @Date: 2019/6/12 14:15
 */
public class JDStockParam implements Serializable {

    //省级id
    private Integer provinceId;

    //城市id
    private Integer cityId;

    //城镇id
    private Integer countyId;

    //镇乡id
    private Integer townId;

    //skuId与购买数量，键值对
    private Map<String, Integer> productIds;

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public Integer getTownId() {
        return townId;
    }

    public void setTownId(Integer townId) {
        this.townId = townId;
    }

    public Map<String, Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(Map<String, Integer> productIds) {
        this.productIds = productIds;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
