package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 京东增量更新接口返回DTO对象
 *
 * @author: weihui
 * @Date: 2019/6/14 15:00
 */
public class JDProductListDTO implements Serializable {

    private List<ProductDTO> dataList;

    private Integer totalRows;

    public List<ProductDTO> getDataList() {
        return dataList;
    }

    public void setDataList(List<ProductDTO> dataList) {
        this.dataList = dataList;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
}
