package com.ddyh.product.admin.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.product.admin.common.annotation.PassToken;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.facade.BackUserFacade;
import com.ddyh.product.facade.param.BackUserParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 16:23
 */
@RestController
@RequestMapping("/")
public class UserController extends BaseController {

    @Reference
    private BackUserFacade backUserFacade;

    @PassToken
    @RequestMapping("login")
    public Result login(@Valid @RequestBody BackUserParam backUserParam) {
        Result result = backUserFacade.backUserLogin(backUserParam);
        if (ResultCode.SUCCESS.getCode().equals(result.getCode())) {
            JdBackUserDTO userDto = (JdBackUserDTO) result.getData();
            String token = getToken(userDto);
            result.setData(token);
        }
        return result;
    }
}
