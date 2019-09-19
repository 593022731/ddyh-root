package com.ddyh.product.service.services;

import com.ddyh.product.dao.mapper.JDGiftProductMapper;
import com.ddyh.product.dao.model.JDGiftProduct;
import com.ddyh.product.facade.param.JDGiftProductQueryParam;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: weihui
 * @Date: 2019/8/15 10:26
 */
@Service("jdGiftProductService")
public class JDGiftProductService {

    @Resource
    private JDGiftProductMapper jdGiftProductMapper;

    public int save(JDGiftProduct item) {
        return jdGiftProductMapper.insert(item);
    }

    public int update(JDGiftProduct item) {
        return jdGiftProductMapper.update(item);
    }

    public int update(Integer id, Integer state) {
        return jdGiftProductMapper.updateState(id, state);
    }

    public JDGiftProduct get(Integer id) {
        return jdGiftProductMapper.get(id);
    }


    public List<JDGiftProduct> getList(JDGiftProductQueryParam param) {
        return jdGiftProductMapper.getList(param);
    }

    public int updateSku(Integer id) {
        return jdGiftProductMapper.updateSku(id);
    }
}
