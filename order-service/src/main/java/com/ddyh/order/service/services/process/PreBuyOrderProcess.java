package com.ddyh.order.service.services.process;

import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.constant.OrderTypeEnum;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.core.BaseOrderCoreService;
import com.ddyh.order.service.services.core.OrderPipeline;
import com.ddyh.order.service.services.validator.Validator;
import org.springframework.stereotype.Service;

/**
 * 团购订单处理器
 * @author: weihui
 * @Date: 2019/8/26 16:22
 */
@Service("prebuyOrderHandler")
public class PreBuyOrderProcess extends BaseOrderCoreService {


    @Override
    public OrderContext createContext(OrderParam param) {
        return null;
    }

    @Override
    public void doBuild(OrderPipeline pipeline) {
        //TODO 构建需要的执行器
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
        return null;
    }

    @Override
    public Integer getOrderType() {
        return OrderTypeEnum.PRE_BUY.getType();
    }
}
