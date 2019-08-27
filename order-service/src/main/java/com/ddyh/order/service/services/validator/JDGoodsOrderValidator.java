package com.ddyh.order.service.services.validator;

import com.ddyh.order.service.services.context.OrderContext;
import org.springframework.stereotype.Service;

@Service("jdGoodsOrderValidator")
public class JDGoodsOrderValidator extends BaseValidator {

    @Override
    public void specialValidate(OrderContext context) {
        //TODO  校验价格
        //TODO  校验库存
    }
}