package com.ddyh.product.facade.facade;

import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDAddressListDTO;

/**
 * 京东地址服务
 *
 * @author: weihui
 * @Date: 2019/6/25 14:17
 */
public interface JDAddressFacade {
    Result<JDAddressListDTO> getProvinceList();

    Result<JDAddressListDTO> getCityListByProvince(Integer provinceId);

    Result<JDAddressListDTO> getCountyListByCity(Integer cityId);

    Result<JDAddressListDTO> getTownListByCounty(Integer countyId);
}
