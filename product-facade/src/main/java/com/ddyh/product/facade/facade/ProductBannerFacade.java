package com.ddyh.product.facade.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.ProductBannerDTO;

import java.util.List;

/**
 * 商品banner
 *
 * @author: weihui
 * @Date: 2019/6/25 15:27
 */
public interface ProductBannerFacade {

    Result<List<ProductBannerDTO>> getProductBannerList();
}
