package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.utils.BeanConvertorUtils;
import com.ddyh.product.dao.model.ProductBanner;
import com.ddyh.product.facade.dto.ProductBannerDTO;
import com.ddyh.product.facade.facade.ProductBannerFacade;
import com.ddyh.product.service.services.ProductBannerService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service(interfaceClass = ProductBannerFacade.class)
public class ProductBannerFacadeImpl implements ProductBannerFacade {

    @Autowired
    private ProductBannerService productBannerService;

    @Override
    public Result<List<ProductBannerDTO>> getProductBannerList() {
        List<ProductBanner> list = productBannerService.getList();
        List<ProductBannerDTO> productBannerDTOS = BeanConvertorUtils.copyList(list, ProductBannerDTO.class);
        return new Result<>(productBannerDTOS);
    }
}
