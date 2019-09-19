package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ddyh.commons.param.PageParam;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.BeanConvertorUtils;
import com.ddyh.product.dao.model.JdProductRecommend;
import com.ddyh.product.facade.constant.ProductConstant;
import com.ddyh.product.facade.dto.JDProductListDTO;
import com.ddyh.product.facade.dto.ProductDTO;
import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.facade.param.CustomSortUpdateParam;
import com.ddyh.product.facade.param.LabelUpdateParam;
import com.ddyh.product.facade.param.ProductParam;
import com.ddyh.product.facade.param.StateUpdateParam;
import com.ddyh.product.service.common.utils.JDResult;
import com.ddyh.product.service.common.utils.JDUtils;
import com.ddyh.product.service.common.utils.URLConstant;
import com.ddyh.product.service.services.ProductService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商品facade实现类
 *
 * @author: weihui
 * @Date: 2019/6/10 11:48
 */
@Service
public class ProductFacadeImpl implements ProductFacade {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProductService productService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${removeUpdateProduct}")
    private String removeUpdateProduct;

    @Value("${expCardCompareVal}")
    private String expCardCompareVal;

    @Override
    public Result<PageResult<ProductDTO>> getProductList(ProductParam param) {
        PageHelper.startPage(param.getCurrentPage(), param.getPageSize(), true);
        PageHelper.orderBy(param.getOrderBy());
        Page<JdProductRecommend> page = (Page<JdProductRecommend>) productService.getList(param);
        long total = page.getTotal();
        List<JdProductRecommend> list = page.getResult();
        List<ProductDTO> data = BeanConvertorUtils.copyList(list, ProductDTO.class);
        PageResult<ProductDTO> pageResult = new PageResult<>(total, data);
        return new Result<>(pageResult);
    }

    @Override
    public Result updateState(StateUpdateParam updateParam) {
        Result result = new Result();
        int updateNum = productService.updateState(updateParam);
        if (updateNum != 1) {
            result = new Result(ResultCode.FAIL);
        }
        return result;
    }

    @Transactional
    @Override
    public Result updateLabel(LabelUpdateParam updateParam) {
        Result result = new Result();
        if (ProductConstant.EXP_CARD__TRUE.equals(updateParam.getExperienceCardInvite())) {
            JdProductRecommend jdProductRecommend = productService.get(updateParam.getSku());
            if (new BigDecimal(jdProductRecommend.getJdPrice()).subtract(new BigDecimal(jdProductRecommend.getMemberPrice())).compareTo(new BigDecimal(expCardCompareVal)) < 0) {
                return new Result(ResultCode.PRICE_BETWEEN_NOT_OK.getCode(), ResultCode.PRICE_BETWEEN_NOT_OK.getMsg() + expCardCompareVal);
            }
        }
        int updateNum = productService.updateLabel(updateParam);
        if (updateNum != 1) {
            result = new Result(ResultCode.FAIL);
        } else {
            //每次编辑超级尖货标签，会重新计算会员价
            setMember(updateParam.getSku());
        }
        return result;
    }

    private void setMember(Long sku) {
        JdProductRecommend item = this.productService.get(sku);
        BigDecimal memberPrice = new BigDecimal(item.getPurchasePrice()).add(new BigDecimal(item.getProfit() * 0.4));//会员价=京东供货价+  (京东零售价 - 京东供货价)*40%

        if (item.getPointedCargo() == 1 || item.getRecommendType() == 1) {
            //超级尖货需要重新计算会员价格
            memberPrice = new BigDecimal(item.getPurchasePrice() * 1.01);//会员价=京东供货价*1.01
//            memberPrice = new BigDecimal(item.getPurchasePrice()).add(new BigDecimal(item.getProfit() * 0.1));//会员特卖价=京东供货价+  (京东零售价 - 京东供货价)*10%
        }
        item.setMemberPrice(memberPrice.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());//会员价
        item.setPrice(item.getMemberPrice());//price存储会员价

        //会员折扣率=会员价/京东零售价
        BigDecimal bigDecima = new BigDecimal(item.getMemberPrice() / item.getJdPrice());
        Double memDiscountRate = bigDecima.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        item.setMemDiscountRate(memDiscountRate);
        this.productService.updateProduct(item);
    }


    @Override
    public Result<List<ProductDTO>> getProductListBySkus(List<Long> skus) {
        ProductParam param = new ProductParam();
        // 批量接口，  不需要加上状态限制
//        param.setState(ProductConstant.STATE_ON);
        param.setSkus(skus);
        List<JdProductRecommend> list = productService.getList(param);

        List<ProductDTO> returnList = new ArrayList<>();
        if (skus.size() > 0) {
            for (Long sku : skus) {//为空的也返回
                ProductDTO noItem = new ProductDTO();
                noItem.setSku(sku);
                noItem.setState(0);
                returnList.add(noItem);
            }
        }

        for (ProductDTO item : returnList) {
            for (JdProductRecommend db : list) {
                if (db.getSku().equals(item.getSku())) {
                    BeanConvertorUtils.copy(db, item);
                }
            }
        }
        return new Result<>(returnList);
    }

    @Override
    public List<ProductDTO> getProductListBySkus(String skus) {
        ProductParam param = new ProductParam();
        List<Long> skuList = new ArrayList<>();
        for (String sku : skus.split(",")) {
            skuList.add(Long.valueOf(sku));
        }
        param.setSkus(skuList);
        List<JdProductRecommend> list = productService.getList(param);
        List<ProductDTO> data = BeanConvertorUtils.copyList(list, ProductDTO.class);
        return data;
    }

    @Override
    public void updateDbProdcut() {
        updateDbProductFromJd();
    }

    @Override
    public ProductDTO get(Long sku) {
        JdProductRecommend item = this.productService.get(sku);
        if (item == null) {
            LOGGER.error("getskuerror=" + sku);
            return null;
        }
        ProductDTO dto = new ProductDTO();
        BeanConvertorUtils.copy(item, dto);
        return dto;
    }


    /**
     * 批量从京东更新db商品数据
     */
    public void updateDbProductFromJd() {
        checkAllProductsHandler(1, 50);
    }


    /**
     * 递归检查所有待更新的京东商品数据
     *
     * @param pageNum
     * @param endNum
     */
    private void checkAllProductsHandler(Integer pageNum, Integer endNum) {
        try {
            PageParam param = new PageParam();
            param.setCurrentPage(pageNum);
            param.setPageSize(endNum);
            JDProductListDTO list = JDUtils.dealGetRequest(URLConstant.PRODUCT_UPDATE, restTemplate,
                    JDProductListDTO.class, false, param);
            if (list != null) {
                List<ProductDTO> jdProductList = list.getDataList();
                if (jdProductList != null && jdProductList.size() > 0) {
                    LOGGER.info("checkAllProductsHandler:pageNum={},size={}", pageNum, jdProductList.size());
                    processData(jdProductList);
                    checkAllProductsHandler(pageNum + 1, endNum);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("checkAllProductsHandlererror={}", e);
            //抛出异常给xxljob
            throw new RuntimeException("checkAllProductsHandlererror");
        }
    }


    @Override
    public void updateProductRealTime(List<Long> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            LOGGER.info("updateProductRealTime skus empty");
            return;
        }
        List<ProductDTO> forUpdateDTOs = new ArrayList<>(skus.size());
        for (Long sku : skus) {
            ProductDTO productDTO = JDUtils.dealSingleParamGetRequest(URLConstant.PRODUCT_DETAIL_REAL_TIME + "?sku={sku}", restTemplate,
                    ProductDTO.class, false, sku);
            forUpdateDTOs.add(productDTO);
        }
        processData(forUpdateDTOs);
    }

    /**
     * （1）要求满足：由运营人工选品、打标签，当前使用超值尖货及首页精选【体验卡邀请】标签的；
     * （2）要求满足：所抓取的商品的（京东推荐价-会员价）>= ￥7.9；
     * （3）排序：按返利值从大往小倒排序；
     */
    @Override
    public Result<PageResult<ProductDTO>> getExpCardProduct(PageParam pageParam) {
        PageHelper.startPage(pageParam.getCurrentPage(), pageParam.getPageSize(), true);
        PageHelper.orderBy("profit desc");
        ProductParam param = new ProductParam();
        param.setExperienceCardInvite(ProductConstant.EXP_CARD__TRUE);
        Page<JdProductRecommend> page = (Page<JdProductRecommend>) productService.getList(param);
        long total = page.getTotal();
        List<JdProductRecommend> list = page.getResult();
        List<ProductDTO> data = BeanConvertorUtils.copyList(list, ProductDTO.class);
        PageResult<ProductDTO> pageResult = new PageResult<>(total, data);
        return new Result<>(pageResult);
    }

    @Override
    public Result updateCustomSort(CustomSortUpdateParam customSortUpdateParam) {

        int updateNum = productService.updateCustomSort(customSortUpdateParam);
        if (updateNum != 1) {
            return new Result(ResultCode.FAIL);
        }
        return new Result();
    }

    /**
     * 商品数据处理
     *
     * @param jdProducts
     */
    private void processData(List<ProductDTO> jdProducts) {
        if (jdProducts != null && jdProducts.size() > 0) {
            List<JdProductRecommend> list = new ArrayList<>();
            StringBuffer skus = new StringBuffer();
            for (ProductDTO jdProduct : jdProducts) {
                skus.append(",").append(jdProduct.getSku());

                JdProductRecommend item = new JdProductRecommend();
                BeanConvertorUtils.copy(jdProduct, item);
                try {
                    item.setImgPath(jdProduct.getImagePath());//设置京东图片
                    item.setJdState(item.getState());//设置京东状态
                    item.setPurchasePrice(item.getPrice());//设置供货价格

                    Integer status = item.getJdState();//商品状态，默认等同于京东状态
                    if (item.getPrice() == null || item.getPrice() <= 0) {//进货价格小于等于0的不上架
                        status = ProductConstant.STATE_CLOSE;
                    }

                    if (item.getJdPrice() == null) {
                        //京东那边出现价格为空，直接下架
                        item.setJdPrice(0.0);
                        status = ProductConstant.STATE_CLOSE;
                    }

                    if (item.getPurchasePrice() == null) {
                        //京东那边出现供货价格为空，直接下架
                        item.setPurchasePrice(0.0);
                        status = ProductConstant.STATE_CLOSE;
                    }

                    //利润=京东零售价-供货价
                    BigDecimal bigDecima = new BigDecimal(item.getJdPrice() - item.getPurchasePrice());
                    item.setProfit(bigDecima.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

                    JdProductRecommend po = this.productService.get(item.getSku());

                    BigDecimal memberPrice = new BigDecimal(item.getPurchasePrice()).add(new BigDecimal(item.getProfit() * 0.4));//会员价=京东供货价+  (京东零售价 - 京东供货价)*40%

                    if (po != null && (po.getPointedCargo() == 1 || po.getRecommendType() == 1)) {
                        //超级尖货需要重新计算会员价格
                        memberPrice = new BigDecimal(item.getPurchasePrice() * 1.01);//会员价=京东供货价*1.01
//                    memberPrice = new BigDecimal(item.getPurchasePrice()).add(new BigDecimal(item.getProfit() * 0.1));//会员特卖价=京东供货价+  (京东零售价 - 京东供货价)*10%
                    }

                    item.setMemberPrice(memberPrice.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());//会员价
                    item.setPrice(item.getMemberPrice());//price存储会员价

                    BigDecimal rate = new BigDecimal(item.getProfit() / item.getJdPrice()).setScale(2, BigDecimal.ROUND_HALF_UP);

                    if (new BigDecimal("0.01").compareTo(rate) == 1) { // 如果 (京东零售价 - 京东供货价)/京东零售价 >=1%，京东的产品才上架 ,反之不上架
                        status = ProductConstant.STATE_CLOSE;
                    }

                    //采购折扣率=京东供货价/京东零售价
                    bigDecima = new BigDecimal(item.getPurchasePrice() / item.getJdPrice());
                    Double discountRate = bigDecima.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    item.setDiscountRate(discountRate);

                    //会员折扣率=会员价/京东零售价
                    bigDecima = new BigDecimal(item.getMemberPrice() / item.getJdPrice());
                    Double memDiscountRate = bigDecima.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    item.setMemDiscountRate(memDiscountRate);


                    if (po == null) {
                        item.setState(status);

                        item.setRequestTime(new Date());
                        item.setRecommendType(0);
                        item.setPointedCargo(0);
                        item.setIsHeightDiscount(0);
                        item.setCustomSort(0);
                        //  this.productService.saveProduct(item);
                    } else {
                        if (status.equals(ProductConstant.STATE_ON)) {//京东状态和价格判断都为上架，则上架，否则就下架
                            status = ProductConstant.STATE_ON;
                        } else {
                            status = ProductConstant.STATE_CLOSE;
                        }
                        item.setState(status);

                        //这5个字段不更新
                        item.setRequestTime(po.getRequestTime());
                        item.setRecommendType(po.getRecommendType());
                        item.setPointedCargo(po.getPointedCargo());
                        item.setIsHeightDiscount(po.getIsHeightDiscount());
                        item.setCustomSort(po.getCustomSort());

                        //this.productService.updateProduct(item);
                    }
                    list.add(item);
                } catch (Exception e) {
                    LOGGER.info("processDataerror,item={}", JSON.toJSON(item));
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            this.productService.replaceInfoProduct(list);
            String skuList = skus.substring(1);
            if ("1".equals(removeUpdateProduct)) {//生产环境才配置1，需要删除京东增量更新的数据
                JDResult result = removeUpdate(skuList);
                LOGGER.info("processData:skus={},result={},error={}", skuList, result.getMsg(), result.getError());
            }
        }
    }

    /**
     * 删除增量同步商品数据
     *
     * @param skus
     * @return
     */
    private JDResult removeUpdate(String skus) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("skus", skus);
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.PRODUCT_REMOVEUPDATE, httpEntity, JDResult.class);
        return JDUtils.requestIsSuccess(entity);
    }

}
