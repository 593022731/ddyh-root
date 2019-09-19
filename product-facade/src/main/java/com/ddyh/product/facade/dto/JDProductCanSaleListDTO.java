package com.ddyh.product.facade.dto;

import java.util.List;

/**
 * @author: cqry2017
 * @Date: 2019/8/6 17:36
 * @descript:
 */
public class JDProductCanSaleListDTO {

    private List<JDProductCanSaleDTO> list;

    private JDProductCanSaleDTO type;

    public List<JDProductCanSaleDTO> getList() {
        return list;
    }

    public void setList(List<JDProductCanSaleDTO> list) {
        this.list = list;
    }

    public JDProductCanSaleDTO getType() {
        return type;
    }

    public void setType(JDProductCanSaleDTO type) {
        this.type = type;
    }
}
