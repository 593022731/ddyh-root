package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.product.facade.dto.WeixinJsConfigDTO;
import com.ddyh.product.facade.facade.WeiXinFacade;
import com.ddyh.product.service.common.utils.WeixinUtil;

import java.util.UUID;

/**
 * @author: weihui
 * @Date: 2019/7/31 11:41
 */
@Service(interfaceClass = WeiXinFacade.class)
public class WeiXinFacadeImpl implements WeiXinFacade {

    @Override
    public WeixinJsConfigDTO getConfig(String returnUrl) {
        String jsapi_ticket = WeixinUtil.getDefaultTicket();

        if (jsapi_ticket == null || jsapi_ticket.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        WeixinJsConfigDTO myBean = new WeixinJsConfigDTO(null);
        myBean.setDebug(false);
        myBean.setAppId(WeixinUtil.WEIXIN_WEB_APPID);
        long timestamp = System.currentTimeMillis() / 1000;
        myBean.setTimestamp(timestamp);
        String nonceStr = UUID.randomUUID().toString();
        myBean.setNonceStr(nonceStr);
        String signature = WeixinUtil.generateConfigSignature(nonceStr, jsapi_ticket, timestamp + "", returnUrl);
        if (signature.isEmpty()) {
            throw new BusinessException(ResultCode.WECHAT_SIGNATURE_ERROR);
        }
        myBean.setSignature(signature);
        return myBean;
    }
}
