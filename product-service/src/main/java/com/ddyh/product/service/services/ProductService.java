package com.ddyh.product.service.services;

import com.ddyh.product.dao.mapper.JdProductRecommendMapper;
import com.ddyh.product.dao.model.JdProductRecommend;
import com.ddyh.product.facade.param.CustomSortUpdateParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: weihui
 * @Date: 2019/6/10 11:58
 */
@Service("productService")
public class ProductService {

    @Autowired
    private JdProductRecommendMapper productMapper;

    /**
     * 保存商品
     *
     * @param item
     * @return
     */
    public int saveProduct(JdProductRecommend item) {
        return productMapper.insert(item);
    }

    /**
     * 修改商品
     *
     * @param item
     * @return
     */
    public int updateProduct(JdProductRecommend item) {
        return productMapper.update(item);
    }


    /**
     * 查询商品
     *
     * @return
     */
    public List<JdProductRecommend> getList(ProductParam param) {
        return productMapper.getList(param);
    }

    /**
     * 更新商品上下架状态
     *
     * @param updateParam
     * @return
     */
    public int updateState(StateUpdateParam updateParam) {
        return productMapper.updateState(updateParam);
    }

    /**
     * 更新商品标签
     *
     * @param updateParam
     * @return
     */
    public int updateLabel(LabelUpdateParam updateParam) {
        return productMapper.updateLabel(updateParam);
    }

    /**
     * 根据sku查询商品
     *
     * @param sku
     * @return
     */
    public JdProductRecommend get(Long sku) {
        return productMapper.get(sku);
    }

    /**
     * 批量 保存/修改 商品
     *
     * @param list
     * @return
     */
    public int replaceInfoProduct(List<JdProductRecommend> list) {
        return productMapper.replaceInfoProduct(list);
    }

    /**
     * 修改京东商品自定义排序值
     *
     * @param updateParam
     * @return
     */
    public int updateCustomSort(CustomSortUpdateParam updateParam) {
        return productMapper.updateCustomSort(updateParam);
    }

}
