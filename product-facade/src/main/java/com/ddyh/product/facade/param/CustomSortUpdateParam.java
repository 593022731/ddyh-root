package com.ddyh.product.facade.param;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/9/6 11:46
 * @descript:
 */
public class CustomSortUpdateParam implements Serializable {

    @NotNull(message = "商品sku不能为空")
    private Long sku;

    @NotNull(message = "排序值不能为空")
    private Integer sortVal;

    public Integer getSortVal() {
        return sortVal;
    }

    public void setSortVal(Integer sortVal) {
        this.sortVal = sortVal;
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
