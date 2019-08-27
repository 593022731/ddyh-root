package com.ddyh.order.facade.param;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *  京东商品订单参数
 * @author: weihui
 * @Date: 2019/8/26 16:38
 */
public class JDGoodsOrderParam extends OrderParam implements Serializable {

    private Integer provinceId;

    private Integer cityId;

    private Integer countyId;

    private Integer townId;

    private String detail;

    private String splicingAddress;

    private String name;

    private String phone;

    private List<JDGooldsOrderItemParam> orderItemVos;

    /**
     * 订单裸价(不包含运费)=所有的商品销售价(京东价/会员价)*对应数量之和
     */
    private BigDecimal orderNakedPrice;

    private Integer freight;

    private String remark;

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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getSplicingAddress() {
        return splicingAddress;
    }

    public void setSplicingAddress(String splicingAddress) {
        this.splicingAddress = splicingAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<JDGooldsOrderItemParam> getOrderItemVos() {
        return orderItemVos;
    }

    public void setOrderItemVos(List<JDGooldsOrderItemParam> orderItemVos) {
        this.orderItemVos = orderItemVos;
    }

    public BigDecimal getOrderNakedPrice() {
        return orderNakedPrice;
    }

    public void setOrderNakedPrice(BigDecimal orderNakedPrice) {
        this.orderNakedPrice = orderNakedPrice;
    }

    public Integer getFreight() {
        return freight;
    }

    public void setFreight(Integer freight) {
        this.freight = freight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}