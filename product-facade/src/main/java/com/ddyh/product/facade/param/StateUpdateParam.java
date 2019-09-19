package com.ddyh.product.facade.param;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/6/11 16:29
 */
public class StateUpdateParam implements Serializable {

    @NotNull(message = "商品sku不能为空")
    private Long sku;

    @NotNull(message = "上架状态不能为空")
    private Integer state;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getSku() {
        return sku;
    }

    public void setSku(Long sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
