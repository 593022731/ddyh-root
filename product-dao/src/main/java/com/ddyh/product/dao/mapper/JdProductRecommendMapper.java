package com.ddyh.product.dao.mapper;

import com.ddyh.product.dao.model.JdProductRecommend;
import com.ddyh.product.facade.param.CustomSortUpdateParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JdProductRecommendMapper {

    /**
     * 批量 保存/修改 商品
     *
     * @param list
     * @return
     */
    int replaceInfoProduct(List<JdProductRecommend> list);

    /**
     * 保存商品数据
     *
     * @param item
     * @return
     */
    int insert(JdProductRecommend item);

    /**
     * 更新商品数据
     *
     * @param item
     * @return
     */
    int update(JdProductRecommend item);

    /**
     * 更新精选推荐、超级尖货标签
     *
     * @param updateParam
     * @return
     */
    int updateLabel(LabelUpdateParam updateParam);

    /**
     * 更新状态上下架
     * @param updateParam
     * @return
     */
    int updateState(StateUpdateParam updateParam);

    /**
     * 查询商品列表
     * @param param
     * @return
     */
    List<JdProductRecommend> getList(ProductParam param);

    /**
     * 根据sku查询商品详情
     *
     * @param sku
     * @return
     */
    JdProductRecommend get(@Param("sku") Long sku);

    /**
     * 根据sku更新京东商品自定义排序
     *
     * @param updateParam
     * @return
     */
    int updateCustomSort(CustomSortUpdateParam updateParam);
}