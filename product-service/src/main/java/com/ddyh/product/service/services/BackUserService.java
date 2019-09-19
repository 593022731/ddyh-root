package com.ddyh.product.service.services;

import com.ddyh.product.dao.mapper.JdBackUserMapper;
import com.ddyh.product.dao.mapper.OperaLogMapper;
import com.ddyh.product.dao.model.JdBackUser;
import com.ddyh.product.dao.model.OperaLog;
import com.ddyh.product.facade.param.BackUserParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 16:36
 */
@Service
public class BackUserService {

    @Resource
    private JdBackUserMapper userMapper;
    @Resource
    private OperaLogMapper logMapper;

    public JdBackUser findBackUser(BackUserParam backUserParam) {
        return userMapper.findBackUser(backUserParam);
    }

    public int saveOperaLog(OperaLog operaLog) {
        return logMapper.insertSelective(operaLog);
    }
}
