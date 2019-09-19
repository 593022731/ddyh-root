package com.ddyh.product.facade.param;

import com.ddyh.commons.param.PageParam;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 京东分类参数
 */
public class JDCategoryParam extends PageParam {

    //分类id
    private Integer catId;

    //分类父级id，第一级为0。非一级分类，必须带上该字段
    private Integer parentId;

    //分类名称
    private String name;

    //分类级别，0，1，2
    private String catClass;

    //是否可用
    private Integer state;

    public Integer getCatId() {
        return catId;
    }

    public void setCatId(Integer catId) {
        this.catId = catId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatClass() {
        return catClass;
    }

    public void setCatClass(String catClass) {
        this.catClass = catClass;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
