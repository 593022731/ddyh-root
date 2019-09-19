package com.ddyh.product.admin.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDCategoryListDTO;
import com.ddyh.product.facade.facade.JDProductCategoryFacade;
import com.ddyh.product.facade.param.JDCategoryParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 京东分类controller
 *
 * @author: weihui
 * @Date: 2019/6/11 16:55
 */
@RestController
@RequestMapping("/jdCategory")
public class JDCategoryController {

    @Reference
    private JDProductCategoryFacade jdProductCategoryFacade;

    /**
     * 商品分类
     *
     * @param param
     * @return
     */
    @GetMapping(value = "/getCategory")
    public Result getCategory(JDCategoryParam param) {
        JDCategoryListDTO dto = jdProductCategoryFacade.getCategory(param);
        return new Result(dto);
    }
}
