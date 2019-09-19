package com.ddyh.product.api.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.product.facade.dto.WeixinJsConfigDTO;
import com.ddyh.product.facade.facade.WeiXinFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 获取微信token，做h5分享之类的功能
 */
@RestController
@RequestMapping("/api/wx")
public class WeiXinController {

    @Reference
    private WeiXinFacade weiXinFacade;

    @GetMapping(value = "jsconfigGet")
    @ResponseBody
    public Result getJsConfig(@RequestParam(value = "return_url", required = false, defaultValue = "") String returnUrl) {
        if (StringUtils.isEmpty(returnUrl)) {
            return ResultUtil.error(ResultCode.PARAM_ERROR);
        }

        WeixinJsConfigDTO result = weiXinFacade.getConfig(returnUrl);
        return ResultUtil.success(result);
    }
}
