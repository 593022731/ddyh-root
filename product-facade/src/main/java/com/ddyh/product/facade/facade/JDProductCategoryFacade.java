package com.ddyh.product.facade.facade;


import com.ddyh.product.facade.dto.JDCategoryListDTO;
import com.ddyh.product.facade.param.JDCategoryParam;

/**
 * 京东分类facade
 *
 * @author: weihui
 * @Date: 2019/6/11 16:40
 */
public interface JDProductCategoryFacade {

    /**
     * 从京东获取分类
     *
     * @param param
     * @return
     */
    JDCategoryListDTO getCategory(JDCategoryParam param);

    /**
     * 从jd获取产品分类(合并返回二三级分类)
     *
     * @param catId
     * @return
     */
    JDCategoryListDTO getProductCategoryFromJD(Integer catId);
}
