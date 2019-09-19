package com.ddyh.product.dao.mapper;


import com.ddyh.product.dao.model.JDGiftProduct;
import com.ddyh.product.facade.param.JDGiftProductQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JDGiftProductMapper {

    int insert(JDGiftProduct record);

    int update(JDGiftProduct record);

    int updateState(@Param("id") Integer id, @Param("state") Integer state);

    JDGiftProduct get(Integer id);

    List<JDGiftProduct> getList(JDGiftProductQueryParam param);

    int updateSku(@Param("id") Integer id);
}