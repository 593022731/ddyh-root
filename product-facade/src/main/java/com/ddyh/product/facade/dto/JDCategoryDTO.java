package com.ddyh.product.facade.dto;

import java.io.Serializable;

/**
 * 京东分类对象
 *
 * @author: weihui
 * @Date: 2019/6/11 16:34
 */
public class JDCategoryDTO implements Serializable {
    //分类id
    private Integer catId;

    //父级id
    private Integer parentId;

    //分类名称
    private String name;

    //分类等级
    private Integer catClass;

    //是否可用
    private Integer state;

    //分类icon
    private String icon;

    //图片
    private String picture;

    //背景
    private String background;

    //三级分类
    private JDCategoryListDTO subCategoryList;

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

    public Integer getCatClass() {
        return catClass;
    }

    public void setCatClass(Integer catClass) {
        this.catClass = catClass;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public JDCategoryListDTO getSubCategoryList() {
        return subCategoryList;
    }

    public void setSubCategoryList(JDCategoryListDTO subCategoryList) {
        this.subCategoryList = subCategoryList;
    }
}
