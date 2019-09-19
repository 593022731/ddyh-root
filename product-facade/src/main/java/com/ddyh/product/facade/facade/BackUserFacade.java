package com.ddyh.product.facade.facade;


import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.dto.OperaLogDto;
import com.ddyh.product.facade.param.BackUserParam;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 16:30
 */
public interface BackUserFacade {

    /**
     * 后台用户用户登录
     *
     * @param userParam
     * @return
     */
    Result backUserLogin(BackUserParam userParam);


    /**
     * 查找后台用户
     *
     * @param userParam
     * @return
     */
    JdBackUserDTO findBackUserByParams(BackUserParam userParam);

    Result saveOperaLog(OperaLogDto operaLogDto);

}
