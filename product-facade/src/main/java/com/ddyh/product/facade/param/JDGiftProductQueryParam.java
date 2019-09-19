package com.ddyh.product.facade.param;

import com.ddyh.commons.param.PageParam;

import java.io.Serializable;

public class JDGiftProductQueryParam extends PageParam implements Serializable {


    private Long channelId;

    /**
     * 1:上架，0:下架
     */
    private Integer state;

    /**
     * 是否公域(0:否，1:是)
     */
    private Integer isPub;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getIsPub() {
        return isPub;
    }

    public void setIsPub(Integer isPub) {
        this.isPub = isPub;
    }
}