package com.ddyh.order.service.services.context;

/**
 * 订单上下文对象
 * @author: weihui
 * @Date: 2019/8/26 16:15
 */
public class OrderContext {

    /** 订单类型*/
    private Integer orderType;

    private Long orderID;

    private String orderNum;

    /**uid*/
    private Long uid;

    /**登录手机号*/
    private String phone;

    /** 角色类型(1:普通用户，2：京卡会员，4：渠道商/联创)*/
    private Short characterType;

    /** 体验卡类型(0代表从未购买，-1代表之前购买过，大于0代表已经是体验卡对应的类型)*/
    private Integer expCardType;

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Short getCharacterType() {
        return characterType;
    }

    public void setCharacterType(Short characterType) {
        this.characterType = characterType;
    }

    public Integer getExpCardType() {
        return expCardType;
    }

    public void setExpCardType(Integer expCardType) {
        this.expCardType = expCardType;
    }
}
