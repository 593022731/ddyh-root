package com.ddyh.product.service.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDStockStateDTO;
import com.ddyh.product.facade.facade.JDStockFacade;
import com.ddyh.product.facade.param.JDStockParam;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: weihui
 * @Date: 2019/6/12 16:29
 */
public class JDStockTest extends TestSupport {

    @Resource
    private JDStockFacade jdStockFacade;

    @Test
    public void getJDProductStockFromCity() {
        Result<List<JDStockStateDTO>> result = jdStockFacade.getJDProductStockFromCity(100401L);
        printLog(result);
    }

    @Test
    public void checkJDProductStock() {
        JDStockParam param = new JDStockParam();
        param.setProvinceId(12);
        param.setCityId(939);
        param.setCountyId(23683);
        param.setTownId(56184);
        Map<String, Integer> productIds = new HashMap<>();
        productIds.put("100000982034", 1);
        param.setProductIds(productIds);
        Result<List<JDStockStateDTO>> result = jdStockFacade.checkJDProductStock(param);
        printLog(result);
    }
}
