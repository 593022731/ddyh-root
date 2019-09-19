package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.product.facade.dto.JDProductAreaLimitResultDTO;
import com.ddyh.product.facade.dto.JDProductDTO;
import com.ddyh.product.facade.facade.JDProductFacade;
import com.ddyh.product.facade.param.JDProductAreaLimitParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 京东商品接口
 *
 * @author: weihui
 * @Date: 2019/6/25 17:17
 */
@RestController
@RequestMapping("/api/jd/product")
public class JDProductController {

    @Reference
    private JDProductFacade jdProductFacade;


    /**
     * 查询jd产品详情
     *
     * @param sku
     * @return
     */
    @GetMapping("/{sku}")
    public Result<JDProductDTO> getProductDetail(@PathVariable("sku") Long sku) {
        JDProductDTO dto = jdProductFacade.getProductDetail(sku);
        return new Result<>(dto);
    }

    /**
     * 查询jd商品图片
     *
     * @param sku
     * @return
     */
    @GetMapping("/img/{sku}")
    public Result<Map<String, Object>> getProductImg(@PathVariable("sku") Long sku) {
        JDProductDTO dto = jdProductFacade.getProductDetail(sku);
        Map<String, Object> result = new HashMap<>();
        result.put("sku", dto.getSku());
        result.put("imagePath", dto.getImagePath());
        return new Result<>(result);
    }

    /**
     * 查询商品是否地区限购
     */
    @GetMapping("/check/areaLimit")
    public Result<List<JDProductAreaLimitResultDTO>> getProductAreaLimit(@Valid JDProductAreaLimitParam areaLimitVo) {
        List<JDProductAreaLimitResultDTO> productDetail = jdProductFacade.getProductAreaLimit(areaLimitVo);
        return ResultUtil.success(productDetail);
    }
}
