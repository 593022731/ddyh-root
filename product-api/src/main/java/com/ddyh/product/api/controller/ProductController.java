package com.ddyh.product.api.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ddyh.commons.param.PageParam;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.product.facade.constant.ProductConstant;
import com.ddyh.product.facade.dto.JDAndDBProductDTO;
import com.ddyh.product.facade.dto.JDProductDTO;
import com.ddyh.product.facade.dto.ProductDTO;
import com.ddyh.product.facade.facade.JDProductFacade;
import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.facade.param.ProductParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 商品接口
 *
 * @author: weihui
 * @Date: 2019/6/25 17:17
 */
@RestController
@RequestMapping("/api/jd/product/db")
public class ProductController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Reference
    private ProductFacade productFacade;

    @Reference
    private JDProductFacade jdProductFacade;

    /**
     * 通过一组sku获取商品列表(目前仅提供给A系统使用)
     *
     * @param skus
     * @return
     */
    @GetMapping("/get/recommend/multi")
    public Result<List<ProductDTO>> getProductListBySkus(@RequestParam("skus") String skus) {
        List<Long> skuList = new ArrayList<>();
        String[] split = skus.split(",");
        for (String sku : split) {
            skuList.add(Long.valueOf(sku));
        }
        Result<List<ProductDTO>> result = productFacade.getProductListBySkus(skuList);
        return result;
    }

    /**
     * 通过sku获得商品详细(京东商品和自己的商品库组合字段一起返回)
     *
     * @param sku
     * @return
     */
    @GetMapping("/get/recommend/{sku}")
    public Result<JDAndDBProductDTO> getRecommendToDb(@PathVariable("sku") Long sku) {
        JDProductDTO jdProduct = jdProductFacade.getProductDetail(sku);
        if (jdProduct == null) {
            LOGGER.error("getRecommendToDberror=" + sku);
            return new Result<>();
        }
        ProductDTO product = productFacade.get(sku);
        JDAndDBProductDTO dto = new JDAndDBProductDTO(jdProduct, product);
        return new Result<>(dto);
    }

    /**
     * 从sonnhe获得一、二、三级分类商品列表
     *
     * @param currentPage
     * @param pageSize
     * @param catClass
     * @param cat0
     * @param recommendType
     * @return
     */
    @GetMapping("/list/recommend/")
    public Result<PageResult<ProductDTO>> getRecommendListFromCat0(Integer currentPage, Integer pageSize,
                                                                   @RequestParam(value = "catClass", defaultValue = "1", required = false) Integer catClass,
                                                                   @RequestParam("cat0") String cat0,
                                                                   @RequestParam("recommendType") Integer recommendType) {
        ProductParam param = new ProductParam();
        setCats(param, cat0, catClass);

        if (recommendType != null && recommendType >= 0) {
            param.setRecommendType(recommendType);
        }

        param.setState(ProductConstant.STATE_ON);
        param.setPageSize(pageSize);
        param.setCurrentPage(currentPage);
        setOrderBy(param);
        return this.productFacade.getProductList(param);
    }

    /**
     * 根据分类+关键字条件搜索
     *
     * @param currentPage
     * @param pageSize
     * @param findName
     * @param cat0
     * @param catClass
     * @return
     */
    @GetMapping("/list/condition")
    public Result<PageResult<ProductDTO>> getListByCondition(Integer currentPage, Integer pageSize, String findName, String cat0, Integer catClass) {
        ProductParam param = new ProductParam();
        setCats(param, cat0, catClass);

        param.setState(ProductConstant.STATE_ON);
        param.setPageSize(pageSize);
        param.setCurrentPage(currentPage);
        param.setKeyword(findName);
        setOrderBy(param);
        return this.productFacade.getProductList(param);
    }

    /**
     * 设置分类条件
     *
     * @param param
     * @param cat
     * @param catClass
     */
    private void setCats(ProductParam param, String cat, Integer catClass) {
        List<Integer> cats = new ArrayList<>();
        if (StringUtils.isNotBlank(cat) && !"-1".equals(cat)) {//精选推荐时app会传入-1代表全部分类
            String[] arr = cat.split(",");
            for (String c : arr) {
                if (StringUtils.isNoneBlank(c.trim())) {
                    cats.add(Integer.valueOf(c));
                }
            }
        }
        if (cats.isEmpty()) {//防止空格传入
            cats = null;
        }
        if (catClass == null || catClass == 1) {//默认查询一级分类
            param.setCat0s(cats);
        } else if (catClass == 2) {//二级分类
            param.setCat1s(cats);
        } else if (catClass == 3) {//三级级分类
            param.setCat2s(cats);
        }
    }

    /**
     * 新的获取超级尖货数据接口
     *
     * @param currentPage
     * @param pageSize
     * @param findName
     * @return
     */
    @GetMapping("/list/getPointedCargoProduct/")
    public Result<PageResult<ProductDTO>> getPointedCargoProductList(Integer currentPage, Integer pageSize, String findName) {
        ProductParam param = new ProductParam();
        param.setPageSize(pageSize);
        param.setCurrentPage(currentPage);
        param.setKeyword(findName);
        param.setPointedCargo(ProductConstant.POINTED_CARGO_TRUE);
        param.setState(ProductConstant.STATE_ON);
        setOrderBy(param);
        return this.productFacade.getProductList(param);
    }

    /**
     * api查询商品通用排序
     *
     * @param param
     */
    private void setOrderBy(ProductParam param) {
        param.setOrderBy("custom_sort desc, discount_rate asc, member_price asc");
    }

    /**
     * 老的获取超级尖货数据接口
     *
     * @param currentPage
     * @param pageSize
     * @param findName
     * @return
     */
    @GetMapping("/list/getPointedCargoProductOld/")
    public Result<PageResult<ProductDTO>> getPointedCargoProductListForOld(Integer currentPage, Integer pageSize, String findName) {
        ProductParam param = new ProductParam();
        param.setPageSize(pageSize);
        param.setCurrentPage(currentPage);
        param.setKeyword(findName);
        param.setPointedCargo(ProductConstant.POINTED_CARGO_FALSE);
        param.setRecommendType(ProductConstant.RECOMMEND_TYPE_FALSE);
        param.setState(ProductConstant.STATE_ON);
        List<Integer> cat0s = new ArrayList<>();
        cat0s.add(1320);
        cat0s.add(1620);
        param.setCat0s(cat0s);
        setOrderBy(param);
        //老的超级尖货逻辑 + 搜索(家居1320和饮料1620一级分类 且非精选商品 且 不是打上标签的，防止重复) j
        return this.productFacade.getProductList(param);
    }

    /**
     * 老的获取精选推荐数据接口
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @GetMapping("/list/getRecommendProductOld/")
    public Result<PageResult<ProductDTO>> getRecommendProductListForOld(Integer currentPage, Integer pageSize) {
        ProductParam param = new ProductParam();
        param.setPageSize(pageSize);
        param.setCurrentPage(currentPage);
        param.setRecommendType(ProductConstant.RECOMMEND_TYPE_FALSE);
        param.setState(ProductConstant.STATE_ON);
        param.setProfitRate(0.35);
        List<Integer> cat0s = new ArrayList<>();
        cat0s.add(1319);
        cat0s.add(1316);
        cat0s.add(1315);
        cat0s.add(6233);
        cat0s.add(16750);
        cat0s.add(11729);
        param.setCat0s(cat0s);
        setOrderBy(param);
        //老的获取精选推荐逻辑(利润 >35%  且 一级分类为(母婴1319，美妆护肤1316，服饰内衣1315，玩具乐器6233，个人护理16750，鞋靴11729) 且 不是打上标签的，防止重复)
        return this.productFacade.getProductList(param);
    }

    /**
     * 邀请体验卡 商品抓取
     *
     * @param pageParam
     * @return
     */
    @GetMapping("/get/exp-card-invite/product")
    public Result<PageResult<ProductDTO>> getExpCardProduct(PageParam pageParam) {
        return productFacade.getExpCardProduct(pageParam);
    }
}
