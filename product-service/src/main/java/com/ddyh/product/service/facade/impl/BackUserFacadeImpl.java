package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.BeanConvertorUtils;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.product.dao.model.JdBackUser;
import com.ddyh.product.dao.model.OperaLog;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.dto.OperaLogDto;
import com.ddyh.product.facade.facade.BackUserFacade;
import com.ddyh.product.facade.param.BackUserParam;
import com.ddyh.product.service.common.utils.Md5Util;
import com.ddyh.product.service.services.BackUserService;

import javax.annotation.Resource;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 16:34
 */

@Service(interfaceClass = BackUserFacade.class)
public class BackUserFacadeImpl implements BackUserFacade {

    @Resource
    private BackUserService backUserService;

    @Override
    public Result backUserLogin(BackUserParam userParam) {
        Result result = ResultUtil.error(ResultCode.USER_NOT_EXIT_OR_PASSWORD_ERROR);
        JdBackUser backUser = backUserService.findBackUser(userParam);
        if (backUser == null) {
            return result;
        }
        String md5Str = Md5Util.md5(userParam.getPassword());
        if (backUser.getPassword() != null && backUser.getPassword().equals(md5Str)) {
            JdBackUserDTO dto = new JdBackUserDTO();
            BeanConvertorUtils.copy(backUser, dto);
            return ResultUtil.success(dto);
        }
        return result;
    }


    @Override
    public JdBackUserDTO findBackUserByParams(BackUserParam userParam) {
        JdBackUser backUser = backUserService.findBackUser(userParam);
        if (backUser == null) {
            return null;
        }
        JdBackUserDTO dto = new JdBackUserDTO();
        BeanConvertorUtils.copy(backUser, dto);
        return dto;
    }

    @Override
    public Result saveOperaLog(OperaLogDto operaLogDto) {
        Result result = ResultUtil.success();
        OperaLog operaLog = new OperaLog();
        BeanConvertorUtils.copy(operaLogDto, operaLog);
        int saveCount = backUserService.saveOperaLog(operaLog);
        if (saveCount != 1) {
            result = ResultUtil.error(ResultCode.FAIL);
        }
        return result;
    }
}
