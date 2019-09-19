package com.ddyh.product.dao.mapper;

import com.ddyh.product.dao.model.JdBackUser;
import com.ddyh.product.facade.param.BackUserParam;

public interface JdBackUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(JdBackUser record);

    int insertSelective(JdBackUser record);

    JdBackUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(JdBackUser record);

    int updateByPrimaryKey(JdBackUser record);

    JdBackUser findBackUser(BackUserParam backUserParam);
}