package com.ddyh.order.service.integration;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.product.dto.JDStockStateDTO;
import com.product.dto.ProductDTO;
import com.product.facade.JDStockFacade;
import com.product.facade.ProductFacade;
import com.product.param.JDStockParam;
import com.product.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品系统服务
 * @author: weihui
 * @Date: 2019/9/2 16:34
 */
@Service
public class ProductFacadeClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Reference
    private ProductFacade productFacade;
    @Reference
    private JDStockFacade jdStockFacade;

    /**
     * 批量查询商品
     * @param skus
     * @return
     */
    public List<ProductDTO> getProductListBySkus(String skus){
        log.info("getProductListBySkus,param={}",skus);
        List<ProductDTO> list = this.productFacade.getProductListBySkus(skus);
        log.info("getProductListBySkus,result={}", JSON.toJSON(list));
        return list;
    }

    /**
     * 查询商品库存、可售、上下架等状态
     * @param stockParam
     * @return
     */
    public Result<List<JDStockStateDTO>>  checkProductState(JDStockParam stockParam){
        log.info("checkProductState,param={}",JSON.toJSON(stockParam));
        Result<List<JDStockStateDTO>> result = jdStockFacade.checkJDProductStockAndLocalState(stockParam);
        log.info("checkProductState,result={}", JSON.toJSON(result));
        return result;
    }

    /**
     * 更新商品数据
     * @param skus
     */
    public void updateProduct(List<Long> skus){
        log.info("updateProduct,param={}",JSON.toJSON(skus));
        productFacade.updateProductRealTime(skus);
    }
}
