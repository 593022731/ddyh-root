package com.ddyh.product.facade.facade;

import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.GiftProductListDTO;
import com.ddyh.product.facade.dto.JDGiftCanSaleDTO;
import com.ddyh.product.facade.dto.JDGiftProductDTO;
import com.ddyh.product.facade.param.JDGiftCanSaleParam;
import com.ddyh.product.facade.param.JDGiftProductParam;
import com.ddyh.product.facade.param.JDGiftProductQueryParam;


/**
 * 京东大礼盒facade
 *
 * @author: weihui
 * @Date: 2019/8/15 16:40
 */
public interface JDGiftProductFacade {

    /**
     * 新增大礼包
     *
     * @param param
     * @return
     */
    Result save(JDGiftProductParam param);

    /**
     * 更新大礼包
     *
     * @param param
     * @return
     */
    Result update(JDGiftProductParam param);

    /**
     * 上下架大礼包
     *
     * @param id
     * @param state 1::上架，0:下架
     * @return
     */
    Result updateState(Integer id, Integer state);

    /**
     * 大礼包详情
     *
     * @param id
     * @return
     */
    Result<JDGiftProductDTO> get(Integer id);

    /**
     * 查询大礼包对应商品列表
     *
     * @param id
     * @return
     */
    Result<GiftProductListDTO> getProductByGiftId(Integer id);

    /**
     * 后台列表查询
     *
     * @param param
     * @return
     */
    PageResult<JDGiftProductDTO> getAdminList(JDGiftProductQueryParam param);

    /**
     * H5 列表查询
     *
     * @param param
     * @return
     */
    Result<PageResult<JDGiftProductDTO>> getList(JDGiftProductQueryParam param);

    /**
     * 大礼包是否可售
     *
     * @param param
     * @return 当canSale = false时，代表不可售，skus字段为空；当canSale = true时，还需要判断skus 是否为空，如果也为空代表可售，不为空，代表返回不可售的skus
     */
    Result<JDGiftCanSaleDTO> getGiftCanSale(JDGiftCanSaleParam param);

}
