package com.ddyh.product.admin.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.facade.JDStockFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 京东库存controller
 *
 * @author: weihui
 * @Date: 2019/6/11 16:55
 */
@RestController
@RequestMapping("/jdStock")
public class JDStockController {

    @Reference
    private JDStockFacade jdStockFacade;

    /**
     * 商品库存
     *
     * @param sku
     * @return
     */
    @GetMapping(value = "/getJDProductStock")
    public Result getJDProductStock(Long sku) {
        return jdStockFacade.getJDProductStockFromCity(sku);
    }
}
