package com.ddyh.product.service.facade;


import com.ddyh.product.facade.dto.JDProductDTO;
import com.ddyh.product.facade.facade.JDProductFacade;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JDProductTest
 *
 * @author: weihui
 * @Date: 2019/6/10 13:54
 */
public class JDProductTest extends TestSupport {

    @Autowired
    private JDProductFacade jdProductFacade;


    @Test
    public void getProductList() {
        JDProductDTO productDetail = jdProductFacade.getProductDetail(2766300L);
        printLog(productDetail);
    }

}
