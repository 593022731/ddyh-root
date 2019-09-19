package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDAddressListDTO;
import com.ddyh.product.facade.facade.JDAddressFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取京东地址接口
 *
 * @author: weihui
 * @Date: 2019/6/25 16:53
 */
@RestController
@RequestMapping("/api/jd/address")
public class JDAddressController {

    @Reference
    private JDAddressFacade jdAddressFacade;

    //获取省列表
    @GetMapping("/provinceList")
    public Result<JDAddressListDTO> getProvinceList() {
        return jdAddressFacade.getProvinceList();
    }

    //根据省id，获取城市列表
    @GetMapping("/province/{provinceId}/cityList")
    public Result<JDAddressListDTO> getCityListByProvince(@PathVariable("provinceId") Integer provinceId) {
        return jdAddressFacade.getCityListByProvince(provinceId);
    }

    //根据城市id，获取市/县列表
    @GetMapping("/city/{cityId}/countyList")
    public Result<JDAddressListDTO> getCountyListByCity(@PathVariable("cityId") Integer cityId) {
        return jdAddressFacade.getCountyListByCity(cityId);
    }

    //根据市/县id，获取镇/乡列表
    @GetMapping("/county/{countyId}/town")
    public Result<JDAddressListDTO> getTownListByCounty(@PathVariable("countyId") Integer countyId) {
        return jdAddressFacade.getTownListByCounty(countyId);
    }
}
