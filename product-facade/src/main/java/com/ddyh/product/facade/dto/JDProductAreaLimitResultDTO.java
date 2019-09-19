package com.ddyh.product.facade.dto;

import java.io.Serializable;

/**
 * @author: cqry2017
 * @Date: 2019/7/1 16:35
 */
public class JDProductAreaLimitResultDTO implements Serializable {
    /**
     * 商品编号
     */
    private Long skuId;
    /**
     * True 区域限制 false 不受区域限制
     */
    private boolean areaRestrict;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public boolean isAreaRestrict() {
        return areaRestrict;
    }

    public void setAreaRestrict(boolean areaRestrict) {
        this.areaRestrict = areaRestrict;
    }
}
