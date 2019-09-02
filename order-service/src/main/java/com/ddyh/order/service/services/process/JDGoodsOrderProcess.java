package com.ddyh.order.service.services.process;

import com.alibaba.fastjson.JSON;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.order.facade.constant.OrderPrefixEnum;
import com.ddyh.order.facade.constant.OrderTypeEnum;
import com.ddyh.order.facade.dto.CreateOrderDTO;
import com.ddyh.order.facade.dto.jd.JDOrderResultDTO;
import com.ddyh.order.facade.dto.jd.JDResultDTO;
import com.ddyh.order.facade.param.JDGoodsOrderParam;
import com.ddyh.order.facade.param.JDGooldsOrderItemParam;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.facade.param.jd.CreateOrderParam;
import com.ddyh.order.service.integration.ProductFacadeClient;
import com.ddyh.order.service.services.context.JDGoodsOrderContext;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.BaseOrderCoreService;
import com.ddyh.order.service.services.core.OrderPipeline;
import com.ddyh.order.service.services.handler.RebateHandler;
import com.ddyh.order.service.services.handler.UpdateOrderHandler;
import com.ddyh.order.service.services.validator.JDGoodsOrderValidator;
import com.ddyh.order.service.services.validator.Validator;
import com.ddyh.order.service.util.JDUrlUtil;
import com.ddyh.order.service.util.JDUtil;
import com.ddyh.user.facade.constant.CharacterTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 京东商品订单处理器
 *
 * @author: weihui
 * @Date: 2019/8/26 16:22
 */
@Service("giftOrderHandler")
public class JDGoodsOrderProcess extends BaseOrderCoreService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ProductFacadeClient productFacadeClient;
    @Resource
    private JDGoodsOrderValidator jdGoodsOrderValidator;
    @Resource
    private UpdateOrderHandler updateOrderHandler;
    @Resource
    private RebateHandler rebateHandler;

    @Override
    public OrderContext createContext(OrderParam param) {
        JDGoodsOrderContext context = new JDGoodsOrderContext();
        context.setParam((JDGoodsOrderParam) param);

        // 计算订单总价格
        short characterType = context.getCharacterType();
        BigDecimal nakedOrderPrice = new BigDecimal(0);
        BigDecimal nakedOrderFloorPrice = new BigDecimal(0);
        for (JDGooldsOrderItemParam orderItemVo : context.getParam().getOrderItemVos()) {
            BigDecimal itemNum = new BigDecimal(orderItemVo.getNum());
            // 计算底价价格
            BigDecimal floorPrice = orderItemVo.getFloorPrice();
            nakedOrderFloorPrice = nakedOrderFloorPrice.add(floorPrice.multiply(itemNum));

            //京卡会员或体验卡会员
            if (characterType > CharacterTypeEnum.USER_NORMAL_TYPE.getType()  || context.getExpCardType() > 0) {
                // 计算平台会员价格
                BigDecimal memberPrice = orderItemVo.getMemberPrice();
                memberPrice = memberPrice.multiply(itemNum);
                nakedOrderPrice = nakedOrderPrice.add(memberPrice);
            } else {
                // 计算平台普通用户价格
                BigDecimal price = orderItemVo.getPlatformPrice();
                price = price.multiply(itemNum);
                nakedOrderPrice = nakedOrderPrice.add(price);
            }
        }

        // 运费
        BigDecimal freight = new BigDecimal(context.getParam().getFreight());
        // 订单裸价
        context.setOrderNakedPrice(context.getParam().getOrderNakedPrice());
        // 计算订单总价 = 订单裸价 + 运费
        context.setOrderPrice(nakedOrderPrice.add(freight));
        // 底价裸价
        context.setOrderFloorNakedPrice(nakedOrderFloorPrice);
        // 底价总价
        context.setOrderFloorPrice(nakedOrderFloorPrice.add(freight));
        // 设置支付价格
        context.setPayPrice(context.getOrderPrice());

        return context;
    }

    @Override
    public void doBuild(OrderPipeline pipeline) {
        //TODO 构建需要的执行器
        pipeline.addLast(updateOrderHandler);
        pipeline.addLast(rebateHandler);
    }

    @Override
    public Result createOrder(OrderContext ctx) {
        JDGoodsOrderContext context = (JDGoodsOrderContext) ctx;
        if (context.getInvalidSku() != null || context.getNoStockSku() != null || context.getUnSaleSku() != null) {
            //不可售的直接返回
            CreateOrderDTO orderDto = new CreateOrderDTO();
            orderDto.setNoStockSku(context.getNoStockSku());
            orderDto.setInvalidSku(context.getInvalidSku());
            orderDto.setUnSaleSku(context.getUnSaleSku());
            return ResultUtil.error(ResultCode.STOCK_NOT_ENOUGH, orderDto);
        }

        //1.先同步给京东订单
        if ("pro".equals(serverEnvs)) {
            //测试环境不需要同步给京东京东
            syncJDOrder(context);
        }


        //TODO 2.再save order suborder orderitem
        String orderNum = createOrderNum(OrderPrefixEnum.JD_GOODS, context.getPhone());

        return null;
    }

    /**
     * 同步给京东订单
     * @param context
     */
    private void syncJDOrder(JDGoodsOrderContext context){
        CreateOrderParam createOrderTo = new CreateOrderParam();
        List<JDGooldsOrderItemParam> orderItemVos = context.getParam().getOrderItemVos();
        Map<String, Integer> map = new HashMap<>();
        for (JDGooldsOrderItemParam orderItemVo : orderItemVos) {
            map.put(Long.toString(orderItemVo.getSkuId()), orderItemVo.getNum());
        }
        createOrderTo.setProducts(map);
        ResponseEntity<JDResultDTO> entity = restTemplate.postForEntity(JDUrlUtil.JD_CREATE_ORDER, createOrderTo, JDResultDTO.class);
        JDOrderResultDTO orderResultTo = JDUtil.dealRequestData(entity, JDOrderResultDTO.class, false);
        // 判断订单价格是否正确
        BigDecimal hqNakedPrice = new BigDecimal(Double.toString(orderResultTo.getOrderPrice()));
        BigDecimal hqTotalPrice = new BigDecimal(Double.toString(orderResultTo.getTotalFee()));
        int compareVal = context.getOrderFloorNakedPrice().compareTo(hqNakedPrice);
        if (compareVal != 0
                || context.getParam().getFreight().compareTo(orderResultTo.getFeight()) != 0
                || context.getOrderFloorPrice().compareTo(hqTotalPrice) != 0) {
            try {
                log.info("dealOrderFromJDerrorjd={}", JSON.toJSONString(orderResultTo));
                log.info("dealOrderFromJDerrormy={}", JSON.toJSONString(context));
                // 请求商品系统触发商品价格实时更新
                List<Long> skus = orderItemVos.stream().map(JDGooldsOrderItemParam::getSkuId).collect(Collectors.toList());
                productFacadeClient.updateProduct(skus);
            } catch (Exception e) {
                log.error("dealOrderFromJD request product update error: {}", e);
            }
            throw new BusinessException(ResultCode.PRICE_CHANGED);
        }
        context.setHqOrderNum(orderResultTo.getOrderSn());
        context.setJdOrderId(Long.parseLong(orderResultTo.getJdTradeNo()));
    }

    @Override
    public void afterProcess(OrderContext context, Result result) {
        //TODO 删除购物车
//        ShoppingCart cart = shoppingCartRepository.findByUserId(currentUser.getUid());
//        for (Long skuId : skuIds) {
//            int delete = shoppingCartItemRepository.deleteByShoppingCartAndSkuId(cart, skuId);
//            if (delete == 1) {
//                log.info("用户 [{}] 的产品 [{}] 从购物车中删除", currentUser.getPhone(), skuId);
//            }
//        }


        //TODO 放redis 未支付的订单
//        String key = context.getOrderNum();
//        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + key;
//        long expireTime = Constant.ORDER_TIMER_EFFECTIVE + 1;
//        redisUtils.set(orderNotPayKey, orderId, expireTime, TimeUnit.MINUTES);
//        orderTimerService.put(orderNotPayKey);
    }

    @Override
    public Validator getValidator() {
        return jdGoodsOrderValidator;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeEnum.JD_GOODS.getType();
    }
}
