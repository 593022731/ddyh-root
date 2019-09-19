package com.ddyh.product.service.facade;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDGiftCanSaleDTO;
import com.ddyh.product.facade.facade.JDGiftProductFacade;
import com.ddyh.product.facade.param.JDGiftCanSaleParam;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JDProductTest
 *
 * @author: weihui
 * @Date: 2019/6/10 13:54
 */
public class JDGiftProductTest extends TestSupport {

    @Autowired
    private JDGiftProductFacade jdGiftProductFacade;


    @Test
    public void getProductList() {
        JDGiftCanSaleParam param = new JDGiftCanSaleParam();
        param.setId(5);
        param.setProvinceId(15);
        param.setCityId(1213);
        param.setCountyId(3038);
        param.setSkus("2219676,4785888,554451");
        Result<JDGiftCanSaleDTO> giftCanSale = jdGiftProductFacade.getGiftCanSale(param);
        System.out.println(JSON.toJSONString(giftCanSale));
    }

}
