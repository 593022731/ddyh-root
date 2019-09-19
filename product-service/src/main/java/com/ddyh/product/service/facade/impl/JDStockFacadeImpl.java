package com.ddyh.product.service.facade.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ddyh.commons.result.Result;
import com.ddyh.product.dao.mapper.JdProductRecommendMapper;
import com.ddyh.product.dao.model.JdProductRecommend;
import com.ddyh.product.facade.constant.ProductConstant;
import com.ddyh.product.facade.constant.StockConstant;
import com.ddyh.product.facade.dto.*;
import com.ddyh.product.facade.facade.JDStockFacade;
import com.ddyh.product.facade.param.JDProductAreaLimitParam;
import com.ddyh.product.facade.param.JDStockParam;
import com.ddyh.product.service.common.utils.JDResult;
import com.ddyh.product.service.common.utils.JDUtils;
import com.ddyh.product.service.common.utils.URLConstant;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 京东库存
 *
 * @author: weihui
 * @Date: 2019/6/12 16:28
 */
@Service(interfaceClass = JDStockFacade.class)
public class JDStockFacadeImpl implements JDStockFacade {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private JdProductRecommendMapper recommendMapper;

    @Override
    public Result<List<JDStockStateDTO>> checkJDProductStock(JDStockParam param) {
        List<JDStockStateDTO> stockStates;
        try {
            if (param.getProductIds() == null || param.getProductIds().isEmpty()) {
                return new Result<>(new ArrayList<>(0));
            }
            ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.STOCK_DETAIL, param, JDResult.class);
            JDResult body = JDUtils.requestIsSuccess(entity);
            Object data = body.getData();
            String jsonStr = JSON.toJSONString(data);
            stockStates = JSONArray.parseArray(jsonStr, JDStockStateDTO.class);
        } catch (Exception e) {
            //有些京东下架商品可能查询失败,相当于无货
            stockStates = buildNoStock(param.getProductIds().keySet());
        }
        return new Result<>(stockStates);
    }

    private List<JDStockStateDTO> buildNoStock(Set<String> skuSet) {
        List<JDStockStateDTO> stockStates = new ArrayList<>(skuSet.size());
        for (String sku : skuSet) {
            JDStockStateDTO stockState = new JDStockStateDTO();
            stockState.setSkuId(Long.valueOf(sku));
            stockState.setStockStateId(StockConstant.NO_SOTCK);
            stockState.setRemainNum(0);
            stockState.setStockStateDesc("无货");
            stockStates.add(stockState);
        }
        return stockStates;
    }

    @Override
    public Result<List<JDStockStateDTO>> getJDProductStockFromCity(Long sku) {
        List<JDStockStateDTO> resList = new ArrayList<>();

        List<JDStockParam> list = buildVo(sku);
        for (JDStockParam param : list) {
            ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.STOCK_DETAIL, param, JDResult.class);
            //有些京东下架商品可能查询失败,相当于无货
            JDResult body = JDUtils.requestIsSuccess(entity);
            Object data = body.getData();
            if (data == null) {
                throw new NullPointerException("转型数据为空");
            }
            String jsonStr = JSON.toJSONString(data);

            List<JDStockStateDTO> stockStates = JSONArray.parseArray(jsonStr, JDStockStateDTO.class);
            for (JDStockStateDTO item : stockStates) {
                String cityName = cityNameMap.get(item.getAreaId());
                item.setAreaName(cityName);
            }
            resList.addAll(stockStates);
        }
        return new Result<>(resList);
    }

    private static Map<String, String> cityNameMap = new HashMap<>();

    static {
        cityNameMap.put("1_2800_2848_0", "北京");
        cityNameMap.put("2_78_51978_0", "上海");
        cityNameMap.put("4_113_9786_0", "重庆");
        cityNameMap.put("3_51035_39620_0", "天津");
        cityNameMap.put("19_1601_3633_0", "广州");
        cityNameMap.put("19_1607_3155_0", "深圳");
        cityNameMap.put("22_1930_4284_0", "成都市");
        cityNameMap.put("15_1213_3411_0", "杭州市");
        cityNameMap.put("17_1381_1386_0", "武汉市");
        cityNameMap.put("12_988_3742_0", "苏州市");
        cityNameMap.put("27_2376_2380_0", "西安市");
        cityNameMap.put("12_904_3373_0", "南京市");
        cityNameMap.put("7_412_415_0", "郑州市");
        cityNameMap.put("18_1482_48936_0", "长沙市");
        cityNameMap.put("8_560_567_0", "沈阳市");
        cityNameMap.put("13_1007_3519_0", "青岛市");
        cityNameMap.put("15_1158_1224_0", "宁波市");
        cityNameMap.put("19_1655_2950_0", "东莞市");
        cityNameMap.put("12_984_3381_0", "无锡市");
    }

    private List<JDStockParam> buildVo(Long sku) {
        List<JDStockParam> list = new ArrayList<>();

        Map<String, Integer> productIds = new HashMap<>();
        productIds.put(sku + "", 1);

        String str = "1,2800,2848;2,78,51978;4,113,9786;3,51035,39620;19,1601,3633;19,1607,3155;22,1930,4284;15,1213,3411;17,1381,1386;12,988,3742;27,2376,2380;12,904,3373;7,412,415;18,1482,48936;8,560,567;13,1007,3519;15,1158,1224;19,1655,2950;12,984,3381";

        String[] arrs1 = str.split(";");
        for (String arr1 : arrs1) {
            String[] arr = arr1.split(",");

            JDStockParam jdStockVo = new JDStockParam();
            jdStockVo.setProvinceId(Integer.valueOf(arr[0]));
            jdStockVo.setCityId(Integer.valueOf(arr[1]));
            jdStockVo.setCountyId(Integer.valueOf(arr[2]));
            jdStockVo.setTownId(0);
            jdStockVo.setProductIds(productIds);
            list.add(jdStockVo);
        }
        return list;
    }


    @Override
    public Result<List<JDStockStateDTO>> checkJDProductStockAndLocalState(JDStockParam param) {
        List<JDStockStateDTO> stockStates;
        try {
            ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.STOCK_DETAIL, param, JDResult.class);
            JDResult body = JDUtils.requestIsSuccess(entity);
            Object data = body.getData();
            String jsonStr = JSON.toJSONString(data);
            stockStates = JSONArray.parseArray(jsonStr, JDStockStateDTO.class);
            if (CollectionUtils.isEmpty(stockStates)) {
                return new Result<>(new ArrayList<>(0));
            }
            for (JDStockStateDTO stockState : stockStates) {
                JdProductRecommend dbItem = recommendMapper.get(stockState.getSkuId());
                if (ProductConstant.STATE_CLOSE.equals(dbItem.getState())) {
                    stockState.setStockStateId(StockConstant.PRODUCT_CLOSE_SALE);
                    stockState.setStockStateDesc("商品已下架");
                }
            }
            List<Integer> canNotSaleStateIdList = Lists.newArrayList(StockConstant.PRODUCT_CLOSE_SALE, StockConstant.PRODUCT_UN_SALE, StockConstant.NO_SOTCK);
            // 新增查询是否可售
            checkCanSale(stockStates, canNotSaleStateIdList);
            // 新增查询是否限售
            checkAreaLimit(stockStates, param, canNotSaleStateIdList);
        } catch (Exception e) {
            //有些京东下架商品可能查询失败,相当于无货
            Map<String, Integer> productIds = param.getProductIds();
            if (CollectionUtils.isEmpty(productIds)) {
                return new Result<>(new ArrayList<>(0));
            }
            stockStates = buildNoStock(param.getProductIds().keySet());
        }
        return new Result<>(stockStates);
    }

    private void checkCanSale(List<JDStockStateDTO> stockStateDTOS, List<Integer> canNotSaleStateIdList) {
        if (CollectionUtils.isEmpty(stockStateDTOS)) {
            return;
        }
        // 只查询有库存商品
        List<JDStockStateDTO> stockEnough = filterProducts(stockStateDTOS, canNotSaleStateIdList);
        if (CollectionUtils.isEmpty(stockEnough)) {
            return;
        }
        String sku = Arrays.stream(stockEnough.stream().map(JDStockStateDTO::getSkuId).toArray()).map(String::valueOf).collect(Collectors.joining(","));
        JDProductCanSaleListDTO resultListDTO = JDUtils.dealSingleParamGetRequest(URLConstant.PRODUCT_CAN_SALE + "?sku={sku}", restTemplate,
                JDProductCanSaleListDTO.class, true, sku);
        List<JDProductCanSaleDTO> canSaleDTOS = resultListDTO.getList();
        if (CollectionUtils.isEmpty(canSaleDTOS)) {
            return;
        }
        List<Long> unSaleSkuList = canSaleDTOS.stream().filter(canSale -> !canSale.isCanSale()).map(JDProductCanSaleDTO::getSkuId).collect(Collectors.toList());
        wrapStockData(stockStateDTOS, unSaleSkuList);
    }

    private void checkAreaLimit(List<JDStockStateDTO> stockStateDTOS, JDStockParam param, List<Integer> canNotSaleStateIdList) {
        // 只查询可售商品
        List<JDStockStateDTO> stockEnough = filterProducts(stockStateDTOS, canNotSaleStateIdList);
        if (CollectionUtils.isEmpty(stockEnough)) {
            return;
        }
        JDProductAreaLimitResultListDTO resultListDTO;
        JDProductAreaLimitParam areaLimitVo = new JDProductAreaLimitParam();
        BeanUtils.copyProperties(param, areaLimitVo);
        String skus = Arrays.stream(stockEnough.stream().map(JDStockStateDTO::getSkuId).toArray()).map(String::valueOf).collect(Collectors.joining(","));
        areaLimitVo.setSku(skus);
        resultListDTO = JDUtils.dealGetRequest(URLConstant.PRODUCT_AREA_LIMIT, restTemplate,
                JDProductAreaLimitResultListDTO.class, true, areaLimitVo);
        if (resultListDTO == null || CollectionUtils.isEmpty(resultListDTO.getList())) {
            return;
        }
        List<JDProductAreaLimitResultDTO> limitResultDTOS = resultListDTO.getList();
        List<Long> unSaleSkuList = limitResultDTOS.stream().filter(JDProductAreaLimitResultDTO::isAreaRestrict).map(JDProductAreaLimitResultDTO::getSkuId).collect(Collectors.toList());
        wrapStockData(stockStateDTOS, unSaleSkuList);

    }

    private void wrapStockData(List<JDStockStateDTO> stockStateDTOS, List<Long> unSaleSkuList) {
        for (JDStockStateDTO stateDTO : stockStateDTOS) {
            if (stateDTO == null) {
                continue;
            }
            if (unSaleSkuList.contains(stateDTO.getSkuId())) {
                stateDTO.setStockStateId(StockConstant.PRODUCT_UN_SALE);
                stateDTO.setStockStateDesc("该商品不可售");
            }
        }
    }

    private List<JDStockStateDTO> filterProducts(List<JDStockStateDTO> stockStateDTOS, List<Integer> canNotSaleStateIdList) {
        Predicate<JDStockStateDTO> predicate = jdStockStateDTO -> !canNotSaleStateIdList.contains(jdStockStateDTO.getStockStateId());
        List<JDStockStateDTO> canSaleProducts = stockStateDTOS.stream().filter(predicate).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(canSaleProducts)) {
            return null;
        }
        return canSaleProducts;
    }

}
