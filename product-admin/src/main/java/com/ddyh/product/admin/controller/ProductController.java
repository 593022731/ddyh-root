package com.ddyh.product.admin.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.admin.common.annotation.OperaRecord;
import com.ddyh.product.facade.dto.ProductDTO;
import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.facade.param.CustomSortUpdateParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 商品相关controller
 * 
 * @author weihui 2019年6月10日 下午2:57:34
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Reference
    private ProductFacade productFacade;

    /**
     * 商品列表
     * @param param
     * @return
     */
    @GetMapping(value = "/getProductList")
    public Result getProductList(ProductParam param) {
        Result<PageResult<ProductDTO>> result = productFacade.getProductList(param);
        return result;
    }


    /**
     * 频道标签编辑
     * @param labelUpdateParam
     * @return
     */
    @OperaRecord(operationType = "商品管理", operationName = "频道标签编辑")
    @PostMapping(value = "/updateLabel")
    public Result updateLabel(@Valid @RequestBody LabelUpdateParam labelUpdateParam) {
        return productFacade.updateLabel(labelUpdateParam);
    }

    /**
     * 下架状态修改
     * @param stateUpdateParam
     * @return
     */
    @OperaRecord(operationType = "商品管理", operationName = "上下架")
    @PostMapping(value = "/updateState")
    public Result updateState(@Valid @RequestBody StateUpdateParam stateUpdateParam) {
        return productFacade.updateState(stateUpdateParam);
    }


    /**
     * 自定义排序值修改
     *
     * @param customSortUpdateParam
     * @return
     */
    @OperaRecord(operationType = "商品管理", operationName = "自定义排序值修改")
    @PostMapping(value = "/updateCustomSort")
    public Result updateCustomSort(@Valid @RequestBody CustomSortUpdateParam customSortUpdateParam) {
        return productFacade.updateCustomSort(customSortUpdateParam);
    }

//    @OperaRecord(operationType = "商品管理", operationName = "上下架")
//    @PostMapping(value = "/updateState")
//    public Result updateState(HttpServletRequest request) {
//        Long sku = Long.valueOf(request.getParameter("sku"));
//        Integer state = 1;
//        StateUpdateParam stateUpdateParam = new StateUpdateParam();
//        stateUpdateParam.setSku(sku);
//        stateUpdateParam.setState(state);
//        return productFacade.updateState(stateUpdateParam);
//    }

}
