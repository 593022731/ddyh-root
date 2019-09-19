package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author: weihui
 * @Date: 2019/8/15 16:01
 */
public class JDGiftCanSaleDTO implements Serializable {

    /**
     * 是否可售
     */
    Boolean canSale;

    /**
     * 不可售sku
     */
    List<Long> skus;

    public JDGiftCanSaleDTO(Boolean canSale, List<Long> skus) {
        this.canSale = canSale;
        this.skus = skus;
    }

    public Boolean getCanSale() {
        return canSale;
    }

    public void setCanSale(Boolean canSale) {
        this.canSale = canSale;
    }

    public List<Long> getSkus() {
        return skus;
    }

    public void setSkus(List<Long> skus) {
        this.skus = skus;
    }
}
