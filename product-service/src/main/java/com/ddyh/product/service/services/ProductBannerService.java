package com.ddyh.product.service.services;


import com.ddyh.product.dao.mapper.ProductBannerMapper;
import com.ddyh.product.dao.model.ProductBanner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品banner
 *
 * @author: weihui
 * @Date: 2019/6/10 11:58
 */
@Service("productBannerService")
public class ProductBannerService {

    @Resource
    private ProductBannerMapper productBannerMapper;


    /**
     * 查询商品banner
     *
     * @return
     */
    public List<ProductBanner> getList() {
        return productBannerMapper.getList();
    }

}
