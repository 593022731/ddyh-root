package com.ddyh.pay.service.services.core;

import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;

/**
 *
 * @author: weihui
 * @Date: 2019/8/19 16:12
 */
public interface PayCoreService {

    /**
     * 获取(支付/退款)订单信息
     * @param param
     * @return
     */
    Result getRequest(RequestParam param);

    /**
     * 回调处理
     * @param param
     * @return
     */
    Result callback(CallBackParam param);
}
