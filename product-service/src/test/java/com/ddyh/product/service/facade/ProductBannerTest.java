package com.ddyh.product.service.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.ProductBannerDTO;
import com.ddyh.product.facade.facade.ProductBannerFacade;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author: weihui
 * @Date: 2019/6/25 15:32
 */
public class ProductBannerTest extends TestSupport {

    @Autowired
    private ProductBannerFacade productBannerFacade;

    @Test
    public void getProductBannerList() {
        Result<List<ProductBannerDTO>> result = productBannerFacade.getProductBannerList();
        printLog(result);
    }
}
