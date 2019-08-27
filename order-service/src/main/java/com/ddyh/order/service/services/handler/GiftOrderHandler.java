package com.ddyh.order.service.services.handler;

import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.constant.OrderTypeEnum;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.BaseOrderCoreService;
import com.ddyh.order.service.services.core.OrderPipeline;
import com.ddyh.order.service.services.validator.GiftOrderValidator;
import com.ddyh.order.service.services.validator.Validator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 大礼包订单处理器
 * @author: weihui
 * @Date: 2019/8/26 16:22
 */
@Service("giftOrderHandler")
public class GiftOrderHandler extends BaseOrderCoreService {

    @Resource
    private GiftOrderValidator giftOrderValidator;

    @Resource
    private UpdateOrderHandler updateOrderHandler;
    @Resource
    private RebateHandler rebateHandler;
    @Resource
    private MemberHandler memberHandler;

    @Override
    public OrderContext createContext(OrderParam param) {
        return null;
    }

    @Override
    protected void doBuild(OrderPipeline pipeline) {
        //TODO 构建需要的执行器
        pipeline.addLast(updateOrderHandler);
        pipeline.addLast(rebateHandler);
        pipeline.addLast(memberHandler);
    }

    @Override
    public Result createOrder(OrderContext context) {
        return null;
    }

    @Override
    public void afterProcess(OrderContext context, Result result) {

    }

    @Override
    public Validator getValidator() {
        return giftOrderValidator;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeEnum.GIFT.getType();
    }
}
