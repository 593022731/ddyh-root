package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.product.facade.dto.JDCategoryDTO;
import com.ddyh.product.facade.dto.JDCategoryListDTO;
import com.ddyh.product.facade.facade.JDProductCategoryFacade;
import com.ddyh.product.facade.param.JDCategoryParam;
import com.ddyh.product.service.common.utils.JDUtils;
import com.ddyh.product.service.common.utils.URLConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;


/**
 * @author: weihui
 * @Date: 2019/6/11 16:42
 */
@Service(interfaceClass = JDProductCategoryFacade.class)
public class JDProductCategoryFacadeImpl implements JDProductCategoryFacade {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public JDCategoryListDTO getCategory(JDCategoryParam param) {
        return JDUtils.dealGetRequest(URLConstant.CATEGORY_LIST, restTemplate,
                JDCategoryListDTO.class, false, param);
    }

    @Override
    public JDCategoryListDTO getProductCategoryFromJD(Integer catId) {
        JDCategoryParam jdCategoryVo01 = new JDCategoryParam();
        jdCategoryVo01.setCatClass("1");
        jdCategoryVo01.setParentId(catId);
        JDCategoryListDTO categories02 = getCategory(jdCategoryVo01);
        List<JDCategoryDTO> data = categories02.getData();
        for (JDCategoryDTO category : data) {
            JDCategoryParam jdCategoryVo02 = new JDCategoryParam();
            jdCategoryVo02.setCatClass("2");
            jdCategoryVo02.setParentId(category.getCatId());
            JDCategoryListDTO categories03 = getCategory(jdCategoryVo02);
            category.setSubCategoryList(categories03);
        }
        return categories02;
    }
}
