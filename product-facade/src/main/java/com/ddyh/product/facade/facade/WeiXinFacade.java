package com.ddyh.product.facade.facade;


import com.ddyh.product.facade.dto.WeixinJsConfigDTO;

/**
 * @author: weihui
 * @Date: 2019/7/31 11:40
 */
public interface WeiXinFacade {

    /**
     * 获取微信js config
     *
     * @param returnUrl
     * @return
     */
    WeixinJsConfigDTO getConfig(String returnUrl);
}
