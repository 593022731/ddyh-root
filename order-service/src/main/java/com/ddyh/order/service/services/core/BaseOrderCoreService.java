package com.ddyh.order.service.services.core;

import com.ddyh.commons.result.Result;
import com.ddyh.order.facade.param.OrderParam;
import com.ddyh.order.service.services.context.OrderContext;
import com.ddyh.order.service.services.validator.Validator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: weihui
 * @Date: 2019/8/26 16:03
 */
@Service
public abstract class BaseOrderCoreService implements OrderCoreService {

    public static Map<Integer, OrderCoreService> orderMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        orderMap.put(getOrderType(), this);
    }

    @Override
    public Result createOrder(OrderParam param) {
        //1.创建上下文
        OrderContext context = createContext(param);
        //2.验证
        getValidator().validate(context);
        //3.下单业务
        Result result = createOrder(context);
        //4..后续逻辑处理
        afterProcess(context,result);
        return result;
    }

    @Override
    public  Result callBackOrder(OrderParam param){
        //1.创建上下文
        OrderContext context = createContext(param);
        //2.创建管道
        OrderPipeline pipeline = new DefaultOrderPipeline(context);
        //3.子类构建需要执行的管道对象
        doBuild(pipeline);
        //4.执行
        pipeline.start();
        //5.获取执行结果
        context = pipeline.getContext();

        return null;
    }

    /**
     * 构建管道
     * @param pipeline
     */
    protected abstract void doBuild(OrderPipeline pipeline);

    /**
     * 核心下单业务
     * @param context
     * @return
     */
    public abstract Result createOrder(OrderContext context);

    /**
     * 获取验证器
     * @return
     */
    public abstract Validator getValidator();

    /**
     * 后续逻辑处理
     * @param context
     * @param result
     * @return
     */
    public abstract void afterProcess(OrderContext context,Result result);

    /**
     * 创建上下文
     * @param param
     * @return
     */
    public abstract OrderContext createContext(OrderParam param);

    /**
     * 获取订单类型
     * @return
     */
    public abstract Integer getOrderType();
}
