package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDAddressListDTO;
import com.ddyh.product.facade.facade.JDAddressFacade;
import com.ddyh.product.service.common.utils.JDUtils;
import com.ddyh.product.service.common.utils.URLConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@Service(interfaceClass = JDAddressFacade.class)
public class JDAddressFacadeImpl implements JDAddressFacade {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Result<JDAddressListDTO> getProvinceList() {
        JDAddressListDTO dto = JDUtils.dealSingleParamGetRequest(URLConstant.PROVINCE_LIST, restTemplate, JDAddressListDTO.class, false);
        return new Result<>(dto);
    }

    @Override
    public Result<JDAddressListDTO> getCityListByProvince(Integer provinceId) {
        JDAddressListDTO dto = JDUtils.dealSingleParamGetRequest(URLConstant.CITY_LIST + "?provinceId={provinceId}", restTemplate, JDAddressListDTO.class, false, provinceId);
        return new Result<>(dto);
    }

    @Override
    public Result<JDAddressListDTO> getCountyListByCity(Integer cityId) {
        JDAddressListDTO dto = JDUtils.dealSingleParamGetRequest(URLConstant.COUNTY_LIST + "?cityId={cityId}", restTemplate, JDAddressListDTO.class, false, cityId);
        return new Result<>(dto);
    }

    @Override
    public Result<JDAddressListDTO> getTownListByCounty(Integer countyId) {
        JDAddressListDTO dto = JDUtils.dealSingleParamGetRequest(URLConstant.TOWN_LIST + "?countyId={countyId}", restTemplate, JDAddressListDTO.class, false, countyId);
        return new Result<>(dto);
    }
}
