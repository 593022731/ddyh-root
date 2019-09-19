package com.ddyh.product.facade.dto;

import java.io.Serializable;

/**
 * 京东地址DTO
 */
public class JDAddressDTO implements Serializable {

    //镇乡名称
    private String name;

    //镇乡id
    private Integer townId;

    //城镇id
    private Integer countyId;

    //城市id
    private Integer cityId;

    //省级id
    private Integer provinceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTownId() {
        return townId;
    }

    public void setTownId(Integer townId) {
        this.townId = townId;
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
}