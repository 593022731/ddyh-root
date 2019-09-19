package com.ddyh.product.service.facade;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.constant.ProductConstant;
import com.ddyh.product.facade.dto.JDProductAreaLimitResultDTO;
import com.ddyh.product.facade.dto.JDProductCanSaleDTO;
import com.ddyh.product.facade.dto.JDProductDTO;
import com.ddyh.product.facade.dto.ProductDTO;
import com.ddyh.product.facade.facade.JDProductFacade;
import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.facade.param.JDProductAreaLimitParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * ProductTest
 *
 * @author: weihui
 * @Date: 2019/6/10 13:54
 */
@Transactional//回滚所有增删改
@AutoConfigureMockMvc//注入一个MockMvc实例
public class ProductTest extends TestSupport {

    @Autowired
    private ProductFacade productFaade;
    @Autowired
    private JDProductFacade jdProductFacade;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProductList() {
        ProductParam param = new ProductParam();
        param.setCurrentPage(1);
        param.setPageSize(10);
        param.setOrderBy("discount_rate desc,member_price desc");

        Result<PageResult<ProductDTO>> result = productFaade.getProductList(param);
        logger.info("result: {}", JSON.toJSONString(result));
//        Assert.assertEquals("1", String.valueOf(result.getData().getTotalCount()));
    }

    @Test
    public void getProductListBySkus() {
        List<ProductDTO> list = productFaade.getProductListBySkus("4281032");
        logger.info("result: {}", JSON.toJSONString(list));
    }

    @Test
    public void testUpdateProductLabel() {
        LabelUpdateParam updateParam = new LabelUpdateParam();
        updateParam.setSku(108474L);
        updateParam.setPointedCargo(ProductConstant.POINTED_CARGO_TRUE);
        updateParam.setRecommendType(ProductConstant.RECOMMEND_TYPE_FALSE);
        Result r = productFaade.updateLabel(updateParam);
        logger.info("result: {}", r);
    }

    @Test
    public void testUpdateState() {
        StateUpdateParam updateParam = new StateUpdateParam();
        updateParam.setSku(108474L);
        updateParam.setState(ProductConstant.STATE_CLOSE);
        Result r = productFaade.updateState(updateParam);
        logger.info("result: {}", r);
    }

    @Test
    public void testApplyCanSale() {
        JDProductDTO productDetail = jdProductFacade.getProductDetail(108474L);
        System.out.println(JSON.toJSONString(productDetail));
        List<JDProductCanSaleDTO> jdProductCanSaleDTOS = jdProductFacade.checkJDProductCanSale(Collections.singletonList(108474L));
        System.out.println(JSON.toJSONString(jdProductCanSaleDTOS));
    }

    @Test
    public void testAreaLimit() {
        JDProductAreaLimitParam limitParam = new JDProductAreaLimitParam();
        limitParam.setCityId(2800);
        limitParam.setCountyId(2848);
        limitParam.setProvinceId(1);
        limitParam.setSku(2938017 + "");
        limitParam.setTownId(0);
        List<JDProductAreaLimitResultDTO> productAreaLimit = jdProductFacade.getProductAreaLimit(limitParam);
        System.out.println(JSON.toJSONString(productAreaLimit));
    }

    //    @Test
////    @Rollback(false)//取消回滚
//    public void getSignature() throws Exception {
//        String responseString = mockMvc.perform(
//                post("/ding/getSignature")//请求的url,请求的方法是post
//                        .contentType(MediaType.APPLICATION_JSON)//数据的格式
//                        .param("url", "https://www.baidu.com/")//添加参数
//        ).andExpect(status().isOk())//返回的状态是200
//                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value(1)) //判断某返回值是否符合预期
//                .andDo(print())//打印出请求和相应的内容
//                .andReturn().getResponse().getContentAsString();//将相应的数据转换为字符串
//        logger.info("post方法/ding/getSignature,{}", responseString);
//    }

//    @Test
//    public void dataUpdate() {
//        Result r = productFaade.dataUpdate();
//        printLog(r);
//    }
}
