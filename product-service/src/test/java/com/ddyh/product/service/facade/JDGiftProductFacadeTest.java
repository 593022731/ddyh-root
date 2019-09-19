package com.ddyh.product.service.facade;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.constant.JDGiftProductConstant;
import com.ddyh.product.facade.dto.GiftProductListDTO;
import com.ddyh.product.facade.dto.JDGiftProductDTO;
import com.ddyh.product.facade.facade.JDGiftProductFacade;
import com.ddyh.product.facade.param.JDGiftProductParam;
import com.ddyh.product.facade.param.JDGiftProductQueryParam;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * @author: cqry2017
 * @Date: 2019/8/29 11:55
 * @descript:
 */
public class JDGiftProductFacadeTest extends TestSupport {

    @Resource
    private JDGiftProductFacade productFacade;

    @Test
    public void getProductByGiftId() {
        Result<GiftProductListDTO> result = productFacade.getProductByGiftId(13);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void getList() {
        JDGiftProductQueryParam param = new JDGiftProductQueryParam();
        param.setChannelId(6L);
        Result<PageResult<JDGiftProductDTO>> list = productFacade.getList(param);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void save() {
        JDGiftProductParam param = new JDGiftProductParam();
        param.setSkus("100401");
        param.setChannelId("5");
        param.setGiftTitle("test");
        param.setGiftPrice(10.0);
        param.setGiftImgpath("123");
        param.setIsPri(0);
        param.setIsPub(1);
        param.setOriginalPrice(1.0);
        param.setPurLimitCity("test");
        param.setSellingPoint("test");
        param.setState(1);
        param.setGiftType(JDGiftProductConstant.GIFT_TYPE);
        productFacade.save(param);
    }
}