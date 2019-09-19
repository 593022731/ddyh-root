package com.ddyh.product.service.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JDAddressListDTO;
import com.ddyh.product.facade.facade.JDAddressFacade;
import com.ddyh.product.service.TestSupport;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: weihui
 * @Date: 2019/6/11 16:44
 */
public class JDAddressTest extends TestSupport {

    @Autowired
    private JDAddressFacade jdAddressFacade;

    @Test
    public void getProvinceList() {
        Result<JDAddressListDTO> result = jdAddressFacade.getProvinceList();
        printLog(result);
    }

    @Test
    public void getTownListByCounty() {
        Result<JDAddressListDTO> result = jdAddressFacade.getTownListByCounty(789);
        printLog(result);
    }
}
