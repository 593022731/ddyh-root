package com.ddyh.product.dao.mapper;


import com.ddyh.product.dao.model.OperaLog;

public interface OperaLogMapper {

    int insertSelective(OperaLog record);

    OperaLog selectByPrimaryKey(Integer id);
}