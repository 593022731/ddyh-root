package com.ddyh.product.facade.param;

import java.io.Serializable;

/**
 * 大礼包可售参数
 *
 * @author: weihui
 * @Date: 2019/8/15 15:32
 */
public class JDGiftCanSaleParam implements Serializable {

    //大礼包id
    private Integer id;

    //商品skus
    private String skus;

    //城镇id
    private Integer countyId;

    //城市id
    private Integer cityId;

    //省级id
    private Integer provinceId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Integer provinceId) {
        this.provinceId = provinceId;
    }

    public String getSkus() {
        return skus;
    }

    public void setSkus(String skus) {
        this.skus = skus;
    }
}
