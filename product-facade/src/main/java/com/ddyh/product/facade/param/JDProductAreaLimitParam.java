package com.ddyh.product.facade.param;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/7/1 16:22
 * 商品是否地区限购查询参数
 */
public class JDProductAreaLimitParam implements Serializable {

    @NotNull(message = "省级id不能为空")
    private Integer provinceId;

    @NotNull(message = "城市id不能为空")
    private Integer cityId;

    @NotNull(message = "市级id不能为空")
    private Integer countyId;

    /**
     * 镇乡id
     */
    private Integer townId;

    @NotNull(message = "商品id不能为空,多个以逗号隔开")
    private String sku;

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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
