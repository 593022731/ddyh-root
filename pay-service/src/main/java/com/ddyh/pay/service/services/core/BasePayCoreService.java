package com.ddyh.pay.service.services.core;

import com.ddyh.commons.result.Result;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: weihui
 * @Date: 2019/8/19 16:15
 */
public abstract class BasePayCoreService implements PayCoreService {

    public static Map<String, PayCoreService> paymentMap = new ConcurrentHashMap<>();

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        paymentMap.put(getPayChannel(), this);
    }

    @Override
    public Result getRequest(RequestParam param) {
        //1.创建上下文
        PaymentContext context = createContext(param);
        //2.验证
        getValidator().validate(context);
        //3.封装参数
        prepare(context);
        //4.业务处理，封装返回对象
        Result result = process(context);
        //5..后续逻辑处理
        afterProcess(context,result);
        return result;
    }

    /**
     * 创建上下文
     * @param param
     * @return
     */
    public abstract PaymentContext createContext(RequestParam param);

    /**
     * 封装处理参数
     * @param context
     * @return
     */
    public abstract void prepare(PaymentContext context);

    /**
     * 获取订单参数处理
     * @param context
     * @return
     */
    public abstract Result process(PaymentContext context);

    /**
     * 获取支付/退款渠道
     * @return
     */
    public abstract String getPayChannel();

    /**
     * 获取验证器
     * @return
     */
    public abstract Validator getValidator();

    /**
     * 后续逻辑处理TODO 保存交易日志记录
     * @param context
     * @param result
     * @return
     */
    public abstract void afterProcess(PaymentContext context,Result result);
}
