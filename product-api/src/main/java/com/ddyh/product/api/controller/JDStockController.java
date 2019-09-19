package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDStockStateDTO;
import com.ddyh.product.facade.facade.JDStockFacade;
import com.ddyh.product.facade.param.JDStockParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 获取京东库存接口
 *
 * @author: weihui
 * @Date: 2019/6/25 17:10
 */
@RestController
@RequestMapping("/api/jd/stock")
public class JDStockController {

    @Reference
    private JDStockFacade jdStockFacade;

    /**
     * 商品库存查询
     *
     * @param param
     * @return
     */
    @PostMapping
    public Result<List<JDStockStateDTO>> checkJDProductStock(@Valid @RequestBody JDStockParam param) {
        return jdStockFacade.checkJDProductStock(param);
    }

    /**
     * 商品库存加状态查询(h5/app/a系统后端 都会调用)
     *
     * @param jdStockVo
     * @return
     */
    @PostMapping(value = "/include-state")
    public Result<List<JDStockStateDTO>> checkJDProductStockAndState(@Valid @RequestBody JDStockParam jdStockVo) {
        return jdStockFacade.checkJDProductStockAndLocalState(jdStockVo);

    }


}
