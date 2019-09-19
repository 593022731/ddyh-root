package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 京东地址返回列表对象
 *
 * @author: weihui
 * @Date: 2019/6/11 16:36
 */
public class JDAddressListDTO implements Serializable {

    private List<JDAddressDTO> data;

    private Integer totalRows;

    public List<JDAddressDTO> getData() {
        return data;
    }

    public void setData(List<JDAddressDTO> data) {
        this.data = data;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
}
