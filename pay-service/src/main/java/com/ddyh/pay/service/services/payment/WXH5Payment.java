package com.ddyh.pay.service.services.payment;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.utils.CommonUtil;
import com.ddyh.commons.utils.HttpClientUtil;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.dto.WXH5PayDTO;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.facade.param.WXPayParam;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.context.WXPaymentContext;
import com.ddyh.pay.service.services.validator.Validator;
import com.ddyh.pay.service.services.validator.WXH5PaymentValidator;
import com.ddyh.pay.service.util.WXUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信H5支付
 *
 * @author: weihui
 * @Date: 2019/8/19 16:17
 */
@Service
public class WXH5Payment extends WXPayment {

    /**
     * 应用id
     */
    @Value("${wx.h5.pay.appId}")
    private String appId;
    /**
     * 商户号
     */
    @Value("${wx.h5.pay.mchId}")
    private String mchId;
    /**
     * 商户密钥key
     */
    @Value("${wx.h5.pay.mchKey}")
    private String mchKey;

    /**
     * 异步回调地址
     */
    @Value("${wx.h5.pay.notifyUrl}")
    private String notifyUrl;

    @Resource
    private WXH5PaymentValidator wxH5PaymentValidator;

    @Override
    public void prepare(PaymentContext context) {
        WXPaymentContext wxPaymentContext = (WXPaymentContext) context;

        SortedMap paraMap = new TreeMap<String, Object>();
        paraMap.put("appid", appId);
        paraMap.put("mch_id", mchId);
        paraMap.put("body", wxPaymentContext.getBody());
        paraMap.put("out_trade_no", wxPaymentContext.getOutTradeNo());
        //单位分
        paraMap.put("total_fee", wxPaymentContext.getTotalFee());
        paraMap.put("spbill_create_ip", wxPaymentContext.getSpbillCreateIp());

        paraMap.put("openid", wxPaymentContext.getOpenId());

        String nonce_str = CommonUtil.getUUID().substring(0, 16);
        paraMap.put("nonce_str", nonce_str);
        paraMap.put("trade_type",wxPaymentContext.getTradeType());
        paraMap.put("notify_url", notifyUrl);
        //调用统一下单前的签名，字段名看文档：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_1
        String sign = WXUtil.createSign(paraMap, mchKey);
        paraMap.put("sign", sign);
        log.info("wxh5preparesign:{}", JSON.toJSONString(paraMap));
        String xml = WXUtil.getRequestXml(paraMap);
        wxPaymentContext.setXml(xml);
    }

    @Override
    public Result process(PaymentContext context) {
        WXPaymentContext wxPaymentContext = (WXPaymentContext) context;
        log.info("WXH5Paymentparam=:{}", wxPaymentContext.getXml());
        String xml = HttpClientUtil.httpPost(UNIFIEDORDER_URL, wxPaymentContext.getXml());
        log.info("WXH5Paymentresult:{}", xml);
        Map<String, String> resultMap = WXUtil.doXMLParse(xml);
        if ("SUCCESS".equals(resultMap.get("result_code"))) {
            String prepayId = resultMap.get("prepay_id");
            //32位长度
            String nonceStr = CommonUtil.getUUID();
            Long currentMills = System.currentTimeMillis();
            //10位长度
            String timeStamp = currentMills.toString().substring(0, 10);
            prepayId = "prepay_id=" + prepayId;

            SortedMap paraMap = new TreeMap<String, Object>();
            paraMap.put("appId", appId);
            paraMap.put("package", prepayId);
            paraMap.put("signType", wxPaymentContext.getSignType());
            paraMap.put("nonceStr", nonceStr);

            paraMap.put("timeStamp",timeStamp);
            //调用微信唤起的时候，要重新签名，注意和统一下单的参数名完全不一样，字段名看文档：https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
            String sign = WXUtil.createSign(paraMap, mchKey);
            log.info("wxh5processsign:{}", JSON.toJSONString(paraMap));

            WXH5PayDTO dto = new WXH5PayDTO(appId, prepayId, wxPaymentContext.getSignType(), nonceStr, timeStamp, sign);
            return new Result(dto);
        }
        String errMsg = resultMap.get("err_code") + ":" + resultMap.get("err_code_des");
        return ResultUtil.error(errMsg);
    }

    @Override
    public Result callback(CallBackParam param) {
        //h5和app的商户key不一样
        super.mchKey = this.mchKey;
        //共用一个回调方法
        return super.callback(param);
    }

    @Override
    public PaymentContext createContext(RequestParam param) {
        WXPayParam wxParam = (WXPayParam) param;
        WXPaymentContext wxPaymentContext = (WXPaymentContext) super.createContext(param);
        wxPaymentContext.setOpenId(wxParam.getOpenId());
        wxPaymentContext.setTradeType("JSAPI");
        wxPaymentContext.setSignType("MD5");
        return wxPaymentContext;
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_H5_PAY.getCode();
    }

    @Override
    public Validator getValidator() {
        return wxH5PaymentValidator;
    }

    @Override
    public void afterProcess(PaymentContext context, Result result) {
        //TODO 生成交易日志表，交易中
    }

}
