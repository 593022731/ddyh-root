package com.ddyh.product.service.facade;


import com.ddyh.product.facade.dto.JDCategoryListDTO;
import com.ddyh.product.facade.facade.JDProductCategoryFacade;
import com.ddyh.product.facade.param.JDCategoryParam;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: weihui
 * @Date: 2019/6/11 16:44
 */
public class JDProductCategoryTest extends TestSupport {

    @Autowired
    private JDProductCategoryFacade jdProductCategoryFacade;

    @Test
    public void getCategory() {
        JDCategoryParam param = new JDCategoryParam();
        param.setCatClass("0");
        JDCategoryListDTO dto = jdProductCategoryFacade.getCategory(param);
        printLog(dto);
    }
}
