package com.ddyh.order.service.services.validator;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.order.facade.param.JDGoodsOrderParam;
import com.ddyh.order.facade.param.JDGooldsOrderItemParam;
import com.ddyh.order.service.integration.ProductFacadeClient;
import com.ddyh.order.service.services.context.JDGoodsOrderContext;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.user.facade.constant.CharacterTypeEnum;
import com.product.constant.StockConstant;
import com.product.dto.JDStockStateDTO;
import com.product.dto.ProductDTO;
import com.product.param.JDStockParam;
import com.product.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("jdGoodsOrderValidator")
public class JDGoodsOrderValidator extends BaseValidator {

    @Resource
    private ProductFacadeClient productFacadeClient;

    @Override
    public void specialValidate(OrderContext context) {
        //校验价格
        validPrice(context);
        //校验库存
        checkStock(context);
    }

    private void validPrice(OrderContext ctx) {
        JDGoodsOrderContext context = (JDGoodsOrderContext) ctx;
        short characterType = context.getCharacterType();
        StringBuffer skus = new StringBuffer();
        for (JDGooldsOrderItemParam orderItemVo : context.getParam().getOrderItemVos()) {
            skus.append(",").append(orderItemVo.getSkuId());
        }

        List<ProductDTO> data = this.productFacadeClient.getProductListBySkus(skus.substring(1).toString());
        Map<Long, ProductDTO> map = new HashMap<>();
        for (ProductDTO dto : data) {
            map.put(dto.getSku(), dto);
        }
        for (JDGooldsOrderItemParam orderItemVo : context.getParam().getOrderItemVos()) {
            Long skuId = orderItemVo.getSkuId();
            ProductDTO productDTO = map.get(skuId);
            if (BigDecimal.valueOf(productDTO.getPurchasePrice()).compareTo(orderItemVo.getFloorPrice()) != 0) {
                throw new BusinessException(ResultCode.PRICE_CHANGED);
            }
            //京卡会员，或体验卡会员
            if (characterType > CharacterTypeEnum.USER_NORMAL_TYPE.getType() || context.getExpCardType() > 0) {
                // 计算平台会员价格
                if (BigDecimal.valueOf(productDTO.getMemberPrice()).compareTo(orderItemVo.getSalePrice()) != 0) {
                    throw new BusinessException(ResultCode.PRICE_CHANGED);
                }
            } else {
                // 计算平台普通用户价格
                if (BigDecimal.valueOf(productDTO.getJdPrice()).compareTo(orderItemVo.getSalePrice()) != 0) {
                    throw new BusinessException(ResultCode.PRICE_CHANGED);
                }
            }
        }
    }

    private void checkStock(OrderContext ctx) {
        JDGoodsOrderContext context = (JDGoodsOrderContext) ctx;
        JDGoodsOrderParam orderInfoVo = context.getParam();
        JDStockParam stockParam = new JDStockParam();
        stockParam.setCityId(orderInfoVo.getCityId());
        stockParam.setCountyId(orderInfoVo.getCountyId());
        stockParam.setProvinceId(orderInfoVo.getProvinceId());
        stockParam.setTownId(orderInfoVo.getTownId());

        List<JDGooldsOrderItemParam> orderItemVos = orderInfoVo.getOrderItemVos();
        Map<String, Integer> ids = new HashMap<>(orderItemVos.size());
        orderItemVos.forEach(orderItemVo -> ids.put(orderItemVo.getSkuId().toString(), orderItemVo.getNum()));
        stockParam.setProductIds(ids);

        // 请求商品系统检查库存
        Result<List<JDStockStateDTO>> listResult = productFacadeClient.checkProductState(stockParam);
        List<JDStockStateDTO> stockResults = listResult.getData();
        if (CollectionUtils.isEmpty(stockResults)) {
            return;
        }
        List<Long> noStockSku = new ArrayList<>();
        List<Long> invalidSku = new ArrayList<>();
        List<Long> unSaleSku = new ArrayList<>();
        stockResults.forEach(stockResult -> {
            if (StockConstant.PRODUCT_CLOSE_SALE == stockResult.getStockStateId().intValue()) {
                //商品下架
                invalidSku.add(stockResult.getSkuId());
            } else if (StockConstant.NO_SOTCK == stockResult.getStockStateId().intValue()) {
                //无库存
                noStockSku.add(stockResult.getSkuId());
            } else if (StockConstant.PRODUCT_UN_SALE == stockResult.getStockStateId().intValue()) {
                unSaleSku.add(stockResult.getSkuId());
            }
        });
        //都为空代表校验通过
        if(invalidSku.isEmpty() && noStockSku.isEmpty() && unSaleSku.isEmpty()){
            return;
        }

        // 无库存
        context.setNoStockSku(noStockSku);
        // 无效
        context.setInvalidSku(invalidSku);
        // 不可售
        context.setUnSaleSku(unSaleSku);
    }
}