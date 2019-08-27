package com.ddyh.order.service.services.context;

import com.ddyh.order.facade.param.JDGoodsOrderParam;

/**
 * 京东商品订单上下文对象
 *
 * @author: weihui
 * @Date: 2019/8/26 16:16
 */
public class JDGoodsOrderContext extends OrderContext {

    private JDGoodsOrderParam param;

    public JDGoodsOrderParam getParam() {
        return param;
    }

    public void setParam(JDGoodsOrderParam param) {
        this.param = param;
    }
}
