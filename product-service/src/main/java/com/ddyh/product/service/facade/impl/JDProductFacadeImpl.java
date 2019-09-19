package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.product.facade.dto.*;
import com.ddyh.product.facade.facade.JDProductFacade;
import com.ddyh.product.facade.param.JDProductAreaLimitParam;
import com.ddyh.product.service.common.utils.JDUtils;
import com.ddyh.product.service.common.utils.URLConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service(interfaceClass = JDProductFacade.class)
public class JDProductFacadeImpl implements JDProductFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDProductFacadeImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JDProductDTO getProductDetail(Long sku) {
        JDProductDTO productDTO = null;
        try {
            productDTO = JDUtils.dealSingleParamGetRequest(URLConstant.PRODUCT_DETAIL + "?sku={sku}",
                    restTemplate, JDProductDTO.class, false, sku);
            if (productDTO == null) {
                return productDTO;
            }
        } catch (Exception e) {
            LOGGER.info("getProductDetailerror,sku={}", sku);
            LOGGER.error("getProductDetailerr={}", e);
            return null;
        }

        // 查询商品是否可售
        List<JDProductCanSaleDTO> canSaleDTOS = checkJDProductCanSale(Collections.singletonList(productDTO.getSku()));
        if (CollectionUtils.isEmpty(canSaleDTOS)) {
            return productDTO;
        }
        JDProductCanSaleDTO canSaleDTO = canSaleDTOS.get(0);
        productDTO.setCanSale(canSaleDTO != null && canSaleDTO.isCanSale());
        return productDTO;
    }

    @Override
    public List<JDProductAreaLimitResultDTO> getProductAreaLimit(JDProductAreaLimitParam areaLimitVo) {
        JDProductAreaLimitResultListDTO resultListDTO;
        try {
            resultListDTO = JDUtils.dealGetRequest(URLConstant.PRODUCT_AREA_LIMIT, restTemplate,
                    JDProductAreaLimitResultListDTO.class, true, areaLimitVo);
        } catch (Exception e) {
            LOGGER.error("getProductAreaLimit params: [{}] error: [{}]", JSON.toJSONString(areaLimitVo), e);
            String[] skuArr = areaLimitVo.getSku().split(",");
            return buildLimit(skuArr);
        }
        return resultListDTO.getList();
    }

    @Override
    public List<JDProductCanSaleDTO> checkJDProductCanSale(List<Long> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String sku = StringUtils.join(skus, ",");
        JDProductCanSaleListDTO resultListDTO = JDUtils.dealSingleParamGetRequest(URLConstant.PRODUCT_CAN_SALE + "?sku={sku}", restTemplate,
                JDProductCanSaleListDTO.class, true, sku);

        return resultListDTO.getList();
    }

    private List<JDProductAreaLimitResultDTO> buildLimit(String[] skuArr) {
        List<JDProductAreaLimitResultDTO> limitResults = new ArrayList<>(skuArr.length);
        for (String sku : skuArr) {
            JDProductAreaLimitResultDTO limitResult = new JDProductAreaLimitResultDTO();
            limitResult.setSkuId(Long.valueOf(sku));
            limitResult.setAreaRestrict(true);
            limitResults.add(limitResult);
        }
        return limitResults;
    }
}
