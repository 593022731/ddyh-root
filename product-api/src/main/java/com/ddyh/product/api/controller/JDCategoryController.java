package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDCategoryListDTO;
import com.ddyh.product.facade.facade.JDProductCategoryFacade;
import com.ddyh.product.facade.param.JDCategoryParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 获取京东分类接口
 *
 * @author: weihui
 * @Date: 2019/6/25 17:05
 */
@RestController
@RequestMapping("/api/sh/productCategory")
public class JDCategoryController {

    @Reference
    private JDProductCategoryFacade jdProductCategoryFacade;

    /**
     * 查询产品分类
     *
     * @param param
     * @return
     */
    @GetMapping("/list")
    public Result<JDCategoryListDTO> getProductCategoryFromJD(@Valid JDCategoryParam param) {
        JDCategoryListDTO dto = jdProductCategoryFacade.getCategory(param);
        return new Result<>(dto);
    }

    /**
     * 查询产品二三级分类
     *
     * @param catId
     * @return
     */
    @GetMapping("/subList")
    public Result<JDCategoryListDTO> subList(@Valid Integer catId) {
        JDCategoryListDTO dto = jdProductCategoryFacade.getProductCategoryFromJD(catId);
        return new Result<>(dto);
    }
}
