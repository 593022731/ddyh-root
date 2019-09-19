package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ddyh.commons.result.PageResult;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.BeanConvertorUtils;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.product.dao.model.JDGiftProduct;
import com.ddyh.product.facade.constant.JDGiftProductConstant;
import com.ddyh.product.facade.constant.StockConstant;
import com.ddyh.product.facade.dto.*;
import com.ddyh.product.facade.facade.JDGiftProductFacade;
import com.ddyh.product.facade.facade.JDStockFacade;
import com.ddyh.product.facade.facade.ProductFacade;
import com.ddyh.product.facade.param.JDGiftCanSaleParam;
import com.ddyh.product.facade.param.JDGiftProductParam;
import com.ddyh.product.facade.param.JDGiftProductQueryParam;
import com.ddyh.product.facade.param.JDStockParam;
import com.ddyh.product.service.services.JDGiftProductService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: weihui
 * @Date: 2019/8/15 10:26
 */
@SuppressWarnings("all")
@Service(interfaceClass = JDGiftProductFacade.class)
public class JDGiftProductFacadeImpl implements JDGiftProductFacade {

    @Resource
    private JDGiftProductService jdGiftProductService;

    @Resource
    private ProductFacade productFacade;

    @Resource
    private JDStockFacade jdStockFacade;


    @Override
    public Result save(JDGiftProductParam param) {
        if (param.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
            //大礼包类型不需要传sku，用主键ID保存
            param.setSkus("");
        }

        String msg = validSkus(param);
        if (StringUtils.isNotBlank(msg)) {
            return ResultUtil.error(msg);
        }

        JDGiftProduct item = new JDGiftProduct();
        BeanConvertorUtils.copy(param, item);
        //默认398不能修改
        item.setGiftPrice(Double.valueOf("398"));
        //默认下架
        item.setState(0);
        jdGiftProductService.save(item);
        if (param.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
            //大礼包用id 保存sku，便于后续查询
            jdGiftProductService.updateSku(item.getId());
        }
        return new Result();
    }

    private String validSkus(JDGiftProductParam param) {
        // 去中文逗号 去空格
        if (StringUtils.isNotEmpty(param.getChannelId())) {
            param.setChannelId(param.getChannelId().replaceAll("，", ",").replaceAll("\\s+", ""));
        }

        if (param.getGiftType().equals(JDGiftProductConstant.JD_GIFT_TYPE)) {
            param.setSkus(param.getSkus().replaceAll("，", ",").replaceAll("\\s+", ""));
            List<ProductDTO> productListBySkus = productFacade.getProductListBySkus(param.getSkus().replaceAll("/", ","));
            if (productListBySkus.isEmpty()) {
                return "商品不存在， 请检查重试!";
            }
            if (productListBySkus.size() != param.getSkus().replaceAll("/", ",").split(",").length) {
                return "部分商品不存在， 请检查重试!";
            }
        }
        return null;
    }


    @Override
    public Result update(JDGiftProductParam param) {
        String msg = validSkus(param);
        if (StringUtils.isNotBlank(msg)) {
            return ResultUtil.error(msg);
        }
        JDGiftProduct jdGiftProduct = jdGiftProductService.get(param.getId());
        if (jdGiftProduct == null) {
            return ResultUtil.error("ID不存在");
        }
        JDGiftProduct item = new JDGiftProduct();
        BeanConvertorUtils.copy(param, item);

        if (param.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
            //大礼包sku不变，还是用主键ID
            item.setSkus(jdGiftProduct.getId() + "");
        }
        //默认398不能修改
        item.setGiftPrice(Double.valueOf("398"));
        jdGiftProductService.update(item);
        return new Result();
    }

    @Override
    public Result updateState(Integer id, Integer state) {
        if (state != 1 && state != 0) {
            return ResultUtil.error(ResultCode.PARAM_ERROR);
        }
        jdGiftProductService.update(id, state);
        return new Result();
    }

    @Override
    public Result<JDGiftProductDTO> get(Integer id) {
        JDGiftProduct jdGiftProduct = jdGiftProductService.get(id);
        JDGiftProductDTO item = new JDGiftProductDTO();
        BeanConvertorUtils.copy(jdGiftProduct, item);
        return new Result(item);
    }

    @Override
    public Result<GiftProductListDTO> getProductByGiftId(Integer id) {
        JDGiftProduct jdGiftProduct = jdGiftProductService.get(id);
        if (jdGiftProduct == null || jdGiftProduct.getState() == 0) {
            return ResultUtil.error("京东大礼盒不存在");
        }
        if (jdGiftProduct.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
            return ResultUtil.error(ResultCode.PARAM_ERROR);
        }

        //必选
        StringBuffer essentialSkus = new StringBuffer();
        //可选
        List<String> limitativeSkus = new ArrayList<>();
        //"1,2,5/6/7,8,11/12,9
        String skus = jdGiftProduct.getSkus();

        String[] arr = skus.split(",");
        for (String sku : arr) {
            if (sku.contains("/")) {
                limitativeSkus.add(sku);
            } else {
                essentialSkus.append(",").append(sku);
            }
        }

        GiftProductListDTO data = new GiftProductListDTO();
        if (StringUtils.isNotBlank(essentialSkus)) {
            List<ProductDTO> list = this.productFacade.getProductListBySkus(essentialSkus.substring(1));
            data.setEssentialList(list);
        }

        //大礼包商品支持可选分组
        //假设后台填写的sku是1,2,3/4/5,6,7/8/9,
        //那么必选节点essentialList会返回一个list[1,2,6],
        //而可选节点limitativeList会返回[[3,4,5],[7,8,9]]
        if (!limitativeSkus.isEmpty()) {
            List<List<ProductDTO>> limitativeList = new ArrayList<>();
            for (String sku : limitativeSkus) {
                String[] split = sku.split("/");
                StringBuffer sb = new StringBuffer();
                for (String s : split) {
                    sb.append(",").append(s);
                }
                List<ProductDTO> list = this.productFacade.getProductListBySkus(sb.substring(1));
                limitativeList.add(list);
            }
            data.setLimitativeList(limitativeList);
        }
        return new Result(data);
    }

    @Override
    public PageResult<JDGiftProductDTO> getAdminList(JDGiftProductQueryParam param) {
        PageHelper.startPage(param.getCurrentPage(), param.getPageSize(), true);
        PageHelper.orderBy("create_time desc");
        Page<JDGiftProduct> page = (Page<JDGiftProduct>) jdGiftProductService.getList(param);
        long total = page.getTotal();
        List<JDGiftProduct> list = page.getResult();
        List<JDGiftProductDTO> data = BeanConvertorUtils.copyList(list, JDGiftProductDTO.class);
        PageResult<JDGiftProductDTO> pageResult = new PageResult<>(total, data);
        return pageResult;
    }

    @Override
    public Result<PageResult<JDGiftProductDTO>> getList(JDGiftProductQueryParam param) {
        param.setState(1);//H5只查询上架的
        List<JDGiftProduct> list = jdGiftProductService.getList(param);
        Set<Integer> set = new HashSet<>();
        for (JDGiftProduct item : list) {
            set.add(item.getId());
        }
        //分开查询，私域和公域，保证我的私域大礼包排序一定是最前面
        //无法使用 PageHelper.orderBy("is_pri desc,is_pub desc");进行排序，如果此公域大礼包是其他的私域，会排序在上面
        param.setChannelId(null);
        param.setIsPub(1);
        List<JDGiftProduct> listPub = jdGiftProductService.getList(param);
        for (JDGiftProduct item : listPub) {
            if (!set.contains(item.getId())) {
                list.add(item);
            }
        }

        long size = (long) list.size();

        List<JDGiftProductDTO> data = BeanConvertorUtils.copyList(list, JDGiftProductDTO.class);
        for (JDGiftProductDTO item : data) {
            if (item.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
                //大礼包直接用title
                item.setProductName(item.getGiftTitle());
            }
        }
        PageResult<JDGiftProductDTO> pageResult = new PageResult<>(size, data);
        return new Result<>(pageResult);
    }

    @Override
    public Result<JDGiftCanSaleDTO> getGiftCanSale(JDGiftCanSaleParam param) {
        JDGiftProduct jdGiftProduct = jdGiftProductService.get(param.getId());
        if (jdGiftProduct == null || jdGiftProduct.getState() == 0) {
            return ResultUtil.error("京东大礼盒不存在");
        }
        boolean result = canSale(jdGiftProduct.getPurLimitId(), param.getProvinceId());

        if (jdGiftProduct.getGiftType().equals(JDGiftProductConstant.GIFT_TYPE)) {
            //大礼包只要校验限售省
            return new Result(new JDGiftCanSaleDTO(result, null));
        } else if (jdGiftProduct.getGiftType().equals(JDGiftProductConstant.JD_GIFT_TYPE)) {
            if (!result) {
                return new Result(new JDGiftCanSaleDTO(result, null));
            }
            if (StringUtils.isBlank(param.getSkus())) {
                return ResultUtil.error("商品sku不能为空");
            }

            //京东大礼盒还需要校验京东库存，可售校验
            JDStockParam storeParam = new JDStockParam();
            storeParam.setProvinceId(param.getProvinceId());
            storeParam.setCityId(param.getCityId());
            storeParam.setCountyId(param.getCountyId());
            storeParam.setTownId(0);
            Map<String, Integer> productIds = new HashMap<>();
            String[] split = param.getSkus().split(",");
            for (String sku : split) {
                productIds.put(sku, 1);
            }
            storeParam.setProductIds(productIds);
            Result<List<JDStockStateDTO>> listResult = jdStockFacade.checkJDProductStockAndLocalState(storeParam);
            List<JDStockStateDTO> stockResults = listResult.getData();
            if (CollectionUtils.isEmpty(stockResults)) {
                return new Result(new JDGiftCanSaleDTO(true, null));
            }
            List<Long> unSaleSku = new ArrayList<>();
            stockResults.forEach(stockResult -> {
                if (StockConstant.NO_SOTCK == stockResult.getStockStateId().intValue() || StockConstant.PRODUCT_CLOSE_SALE == stockResult.getStockStateId().intValue() || StockConstant.PRODUCT_UN_SALE == stockResult.getStockStateId().intValue()) {
                    unSaleSku.add(stockResult.getSkuId());
                }
            });
            return new Result(new JDGiftCanSaleDTO(true, unSaleSku));
        }
        return new Result(new JDGiftCanSaleDTO(false, null));
    }

    private boolean canSale(String purLimitId, Integer provinceId) {
        if (StringUtils.isBlank(purLimitId)) {
            return true;
        }
        String[] arr = purLimitId.split(",");

        for (String pid : arr) {
            if (provinceId.equals(Integer.valueOf(pid))) {
                return false;
            }
        }
        return true;
    }
}
