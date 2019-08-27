package com.ddyh.order.service.services.handler;

import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.constant.OrderTypeEnum;
import com.ddyh.order.facade.param.JDGoodsOrderParam;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.service.services.context.JDGoodsOrderContext;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.BaseOrderCoreService;
import com.ddyh.order.service.services.core.OrderPipeline;
import com.ddyh.order.service.services.validator.JDGoodsOrderValidator;
import com.ddyh.order.service.services.validator.Validator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 京东商品订单处理器
 * @author: weihui
 * @Date: 2019/8/26 16:22
 */
@Service("giftOrderHandler")
public class JDGoodsOrderHandler extends BaseOrderCoreService {

    @Resource
    private JDGoodsOrderValidator jdGoodsOrderValidator;

    @Resource
    private UpdateOrderHandler updateOrderHandler;
    @Resource
    private RebateHandler rebateHandler;

    @Override
    public OrderContext createContext(OrderParam param) {
        JDGoodsOrderContext context = new JDGoodsOrderContext();
        context.setParam((JDGoodsOrderParam)param);
        return context;
    }

    @Override
    protected void doBuild(OrderPipeline pipeline) {
        //TODO 构建需要的执行器
        pipeline.addLast(updateOrderHandler);
        pipeline.addLast(rebateHandler);
    }

    @Override
    public Result createOrder(OrderContext context) {
        //TODO 先同步给京东订单

        //TODO 再save order orderitem，考虑事务


        return null;
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
