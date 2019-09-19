package com.ddyh.product.facade.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDStockStateDTO;
import com.ddyh.product.facade.param.JDStockParam;

import java.util.List;

/**
 * 京东库存facade
 *
 * @author: weihui
 * @Date: 2019/6/12 16:27
 */
public interface JDStockFacade {

    /**
     * 商品库存查询
     *
     * @param param
     * @return
     */
    Result<List<JDStockStateDTO>> checkJDProductStock(JDStockParam param);

    /**
     * 商品库存加本地上下架状态查询
     *
     * @param param
     * @return
     */
    Result<List<JDStockStateDTO>> checkJDProductStockAndLocalState(JDStockParam param);

    /**
     * 查询京东商品几大城市的库存
     *
     * @param sku
     * @return
     */
    Result<List<JDStockStateDTO>> getJDProductStockFromCity(Long sku);
}
