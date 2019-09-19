package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 大礼盒商品list DTO
 *
 * @author: weihui
 * @Date: 2019/6/10 11:06
 */
public class GiftProductListDTO implements Serializable {

    /**
     * 必选商品list
     */
    List<ProductDTO> essentialList;

    /**
     * 可选商品list
     */
    List<List<ProductDTO>> limitativeList;

    public List<ProductDTO> getEssentialList() {
        return essentialList;
    }

    public void setEssentialList(List<ProductDTO> essentialList) {
        this.essentialList = essentialList;
    }

    public List<List<ProductDTO>> getLimitativeList() {
        return limitativeList;
    }

    public void setLimitativeList(List<List<ProductDTO>> limitativeList) {
        this.limitativeList = limitativeList;
    }
}
