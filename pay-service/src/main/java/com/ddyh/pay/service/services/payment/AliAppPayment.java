package com.ddyh.pay.service.services.payment;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.pay.dao.model.TradeLog;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.constant.TradeStatusEnum;
import com.ddyh.pay.facade.constant.TradeTypeEnum;
import com.ddyh.pay.facade.param.AliPayParam;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.service.services.TradeLogService;
import com.ddyh.pay.service.services.context.AliPaymentContext;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.core.BasePayCoreService;
import com.ddyh.pay.service.services.validator.AliPaymentValidator;
import com.ddyh.pay.service.services.validator.Validator;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝APP支付
 *
 * @author: weihui
 * @Date: 2019/8/19 16:17
 */
@Service("aliAppPayment")
public class AliAppPayment extends BasePayCoreService {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String DEFAULT_SIGN_TYPE = "RSA2";
    private static final String gateway = "https://openapi.alipay.com/gateway.do";

    /**
     * 异步回调地址
     */
    @Value("${ali.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 应用id
     */
    @Value("${ali.pay.appId}")
    private String appId;

    /**
     * 支付宝公钥
     */
    @Value("${ali.pay.publicKey}")
    private String publicKey;

    /**
     * 应用私钥
     */
    @Value("${ali.pay.privateKey}")
    private String privateKey;

    private AlipayClient alipayClient;

    @Resource
    private AliPaymentValidator aliPaymentValidator;

    @Resource
    private TradeLogService tradeLogService;

    @Override
    public PaymentContext createContext(RequestParam param) {
        AliPayParam aliParam = (AliPayParam) param;
        AliPaymentContext aliPaymentContext = new AliPaymentContext();
        aliPaymentContext.setPayChannel(aliParam.getPayChannel());
        aliPaymentContext.setSubject(aliParam.getSubject());
        aliPaymentContext.setOutTradeNo(aliParam.getTradeNo());
        aliPaymentContext.setTotalFee(aliParam.getTotalFee());
        return aliPaymentContext;
    }

    @Override
    public void prepare(PaymentContext context) {
        AliPaymentContext aliPaymentContext = (AliPaymentContext) context;
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(aliPaymentContext.getOutTradeNo());
        model.setSubject(aliPaymentContext.getSubject());
//        model.setBody(body);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(aliPaymentContext.getTotalFee().toString());
//        model.setPassbackParams(passbackParams);
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        aliPaymentContext.setRequest(request);

        alipayClient = new DefaultAlipayClient(gateway, appId, privateKey, "json", DEFAULT_CHARSET,
                publicKey, DEFAULT_SIGN_TYPE);
    }

    @Override
    public Result process(PaymentContext context) {
        try {
            AliPaymentContext aliPaymentContext = (AliPaymentContext) context;
            AlipayTradeAppPayRequest request = aliPaymentContext.getRequest();
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse resp = alipayClient.sdkExecute(request);
            log.info("aliPay process={}", JSON.toJSONString(resp));
            String body = resp.getBody();
            return new Result(body);
        } catch (Exception e) {
            throw new BusinessException("支付宝处理支付异常");
        }
    }

    @Override
    public Result callback(CallBackParam param) {
        Map<String, String[]> requestParams = param.getAliParam();

        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        log.info("alipaycallbackresult={}", JSON.toJSONString(params));

        //验签
        if (!verify(params)) {
            throw new BusinessException("支付宝支付验签失败");
        }

        String orderNum = params.get("out_trade_no");
        //已回调，直接return
        TradeLog tradeLog = tradeLogService.get(orderNum);
        if(!tradeLog.getTradeStatus().equals(TradeStatusEnum.COMMIT.getCode())){
            throw new BusinessException(ResultCode.REPEAT_COMMIT);
        }

        //TODO 直接调用订单系统，会处理订单已完成，直接return，根据订单类型自动处理不同类型的订单回调

        //TRADE_FINISH(支付完成)、TRADE_SUCCESS(支付成功)、FAIL(支付失败)
        String tradeStatus = params.get("trade_status").toString();

        log.info("alipaycallback={},{}", orderNum, tradeStatus);
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            //更新交易日志表，交易成功
            tradeLogService.update(orderNum, params.get("trade_no"), params.get("gmt_payment"), TradeStatusEnum.FINISH, "");
            //TODO 更新订单表状态已付款
        } else if ("TRADE_FINISH".equals(tradeStatus)) {
            //TODO 更新订单表状态已付款
        } else if ("FAIL".equals(tradeStatus)) {
            // 更新交易日志表，交易失败TODO 失败原因字段？
            tradeLogService.update(orderNum, params.get("trade_no"), DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"), TradeStatusEnum.FAIL, "");
        } else {
            return ResultUtil.error("fail");
        }
        //返回给支付，让其不在继续回调
        return new Result(orderNum);
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.ALI_APP_PAY.getCode();
    }

    @Override
    public Validator getValidator() {
        return aliPaymentValidator;
    }

    @Override
    public void afterProcess(PaymentContext context, Result result) {
        //生成交易日志表，交易中
        AliPaymentContext aliPaymentContext = (AliPaymentContext) context;
        String tradeNo = aliPaymentContext.getOutTradeNo();
        BigDecimal totalFee = aliPaymentContext.getTotalFee();
        tradeLogService.save(tradeNo, totalFee, PayChannelEnum.ALI_APP_PAY, TradeTypeEnum.PAY);
    }

    /**
     * 支付宝验签
     *
     * @param params
     * @return
     */
    private boolean verify(Map<String, String> params) {
        try {
            //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
            //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
            return AlipaySignature.rsaCheckV1(params, publicKey, DEFAULT_CHARSET, "RSA2");
        } catch (AlipayApiException e) {
            log.error("aliverify,err={}", e);
            return false;
        }
    }


}
