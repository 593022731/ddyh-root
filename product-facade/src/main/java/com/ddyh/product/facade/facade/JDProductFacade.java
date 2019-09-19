package com.ddyh.product.facade.facade;


import com.ddyh.product.facade.dto.JDProductAreaLimitResultDTO;
import com.ddyh.product.facade.dto.JDProductCanSaleDTO;
import com.ddyh.product.facade.dto.JDProductDTO;
import com.ddyh.product.facade.param.JDProductAreaLimitParam;

import java.util.List;

/**
 * 京东商品服务
 *
 * @author: weihui
 * @Date: 2019/6/25 16:30
 */
public interface JDProductFacade {

    /**
     * 从京东获取商品详情并查询是否可售
     *
     * @param sku
     * @return
     */
    JDProductDTO getProductDetail(Long sku);

    /**
     * 查询商品是否地区限购
     */
    List<JDProductAreaLimitResultDTO> getProductAreaLimit(JDProductAreaLimitParam areaLimitVo);

    /**
     * 查询商品是否可售
     *
     * @param sku
     * @return
     */
    List<JDProductCanSaleDTO> checkJDProductCanSale(List<Long> sku);
}
