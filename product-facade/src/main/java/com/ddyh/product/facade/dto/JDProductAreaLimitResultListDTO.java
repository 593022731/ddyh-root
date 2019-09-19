package com.ddyh.product.facade.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author: cqry2017
 * @Date: 2019/7/1 16:35
 */
public class JDProductAreaLimitResultListDTO implements Serializable {

    private List<JDProductAreaLimitResultDTO> list;

    private JDProductAreaLimitResultDTO type;

    public List<JDProductAreaLimitResultDTO> getList() {
        return list;
    }

    public void setList(List<JDProductAreaLimitResultDTO> list) {
        this.list = list;
    }

    public JDProductAreaLimitResultDTO getType() {
        return type;
    }

    public void setType(JDProductAreaLimitResultDTO type) {
        this.type = type;
    }
}
