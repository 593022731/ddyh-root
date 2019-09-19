package com.ddyh.product.service.facade;


import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * ProductTest
 *
 * @author: weihui
 * @Date: 2019/6/10 13:54
 */
@Transactional//回滚所有增删改
@AutoConfigureMockMvc//注入一个MockMvc实例
public class ProductDataUpdateTest extends TestSupport {

    @Resource
    private ProductFacade productFacade;

    @Test
    public void updateProduct() {
        productFacade.updateDbProdcut();
    }

}
