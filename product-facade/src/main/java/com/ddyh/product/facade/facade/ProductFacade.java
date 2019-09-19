package com.ddyh.product.facade.facade;


import com.ddyh.commons.param.PageParam;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.ProductDTO;
import com.ddyh.product.facade.param.CustomSortUpdateParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;

import java.util.List;

/**
 * 商品相关facade
 * @author: weihui
 * @Date: 2019/6/10 11:15
 */
public interface ProductFacade {

    /**
     * 商品管理列表
     * @param param
     * @return
     */
    Result<PageResult<ProductDTO>> getProductList(ProductParam param);

    /**
     * 商品修改相关
     * @param stateUpdateParam
     * @return
     */
    Result updateState(StateUpdateParam stateUpdateParam);

    /**
     * 商品修改相关
     * @param labelUpdateParam
     * @return
     */
    Result updateLabel(LabelUpdateParam labelUpdateParam);

    /**
     * 根据skuList批量获取商品(为空数据也返回)
     *
     * @param skus
     * @return
     */
    Result<List<ProductDTO>> getProductListBySkus(List<Long> skus);

    /**
     * 批量获取sku
     *
     * @param skus
     * @return
     */
    List<ProductDTO> getProductListBySkus(String skus);

    /**
     * 商品详情
     *
     * @param sku
     * @return
     */
    ProductDTO get(Long sku);

    /**
     * 触发db同步京东商品信息
     *
     * @return
     */
    void updateDbProdcut();

    /**
     * 实时更新商品价格
     *
     * @param skus
     */
    void updateProductRealTime(List<Long> skus);

    /**
     * 获取体验卡商品
     *
     * @param pageParam
     * @return
     */
    Result<PageResult<ProductDTO>> getExpCardProduct(PageParam pageParam);

    /**
     * 修改商品自定义排序值
     *
     * @param customSortUpdateParam
     * @return
     */
    Result updateCustomSort(CustomSortUpdateParam customSortUpdateParam);
}
