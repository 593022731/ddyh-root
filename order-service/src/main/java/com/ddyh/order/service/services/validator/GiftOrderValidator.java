package com.ddyh.order.service.services.validator;

import com.ddyh.order.service.services.context.OrderContext;
import org.springframework.stereotype.Service;

@Service("giftOrderValidator")
public class GiftOrderValidator extends BaseValidator {

    @Override
    public void specialValidate(OrderContext context) {

    }
}