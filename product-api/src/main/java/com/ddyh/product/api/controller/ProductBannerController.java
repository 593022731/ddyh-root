package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.ProductBannerDTO;
import com.ddyh.product.facade.facade.ProductBannerFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页顶部轮播接口
 *
 * @author: weihui
 * @Date: 2019/6/25 17:14
 */
@RestController
@RequestMapping("/api/productBanner")
public class ProductBannerController {

    @Reference
    private ProductBannerFacade productBannerFacade;

    /**
     * 获取页顶部轮播列表
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<ProductBannerDTO>> list() {
        return productBannerFacade.getProductBannerList();
    }
}
