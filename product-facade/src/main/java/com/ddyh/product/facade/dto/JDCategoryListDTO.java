package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 京东分类列表对象
 *
 * @author: weihui
 * @Date: 2019/6/11 16:36
 */
public class JDCategoryListDTO implements Serializable {

    private List<JDCategoryDTO> data;

    private Integer totalRows;

    public List<JDCategoryDTO> getData() {
        return data;
    }

    public void setData(List<JDCategoryDTO> data) {
        this.data = data;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }
}
