package com.ddyh.pay.service.services.payment;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.utils.CommonUtil;
import com.ddyh.commons.utils.HttpClientUtil;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.dto.WXPayDTO;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.facade.param.WXPayParam;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.context.WXPaymentContext;
import com.ddyh.pay.service.services.core.BasePayCoreService;
import com.ddyh.pay.service.services.validator.Validator;
import com.ddyh.pay.service.services.validator.WXPaymentValidator;
import com.ddyh.pay.service.util.WXUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 微信APP支付
 *
 * @author: weihui
 * @Date: 2019/8/19 16:17
 */
@Service
public class WXPayment extends BasePayCoreService {

    /**
     * 统一下单请求URL
     */
    private static final String UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 应用id
     */
    @Value("${wx.app.pay.appId}")
    protected String appId;
    /**
     * 商户号
     */
    @Value("${wx.app.pay.mchId}")
    protected String mchId;
    /**
     * 商户密钥key
     */
    @Value("${wx.app.pay.mchKey}")
    protected String mchKey;

    /**
     * 异步回调地址
     */
    @Value("${wx.pay.notifyUrl}")
    protected String notifyUrl;

    @Resource
    private WXPaymentValidator wxPaymentValidator;

    @Override
    public PaymentContext createContext(RequestParam param) {
        WXPayParam wxParam = (WXPayParam) param;
        WXPaymentContext wxPaymentContext = new WXPaymentContext();
        wxPaymentContext.setPayChannel(wxParam.getPayChannel());
        wxPaymentContext.setOutTradeNo(wxParam.getTradeNo());
        wxPaymentContext.setTotalFee(wxParam.getTotalFee());
        wxPaymentContext.setSpbillCreateIp(wxParam.getSpbillCreateIp());
        wxPaymentContext.setTradeType("APP");
        wxPaymentContext.setSignType("Sign=WXPay");
        return wxPaymentContext;
    }

    @Override
    public void prepare(PaymentContext context) {
        WXPaymentContext wxPaymentContext = (WXPaymentContext) context;

        SortedMap paraMap = new TreeMap<String, Object>();
        paraMap.put("appid", appId);
        paraMap.put("mch_id", mchId);
        paraMap.put("body", "东东优汇产品");
        paraMap.put("out_trade_no", wxPaymentContext.getOutTradeNo());
        //单位分
        paraMap.put("total_fee", wxPaymentContext.getTotalFee());
        paraMap.put("spbill_create_ip", wxPaymentContext.getSpbillCreateIp());

        if(StringUtils.isNotBlank(wxPaymentContext.getOpenId())){
            //微信h5支付必填
            paraMap.put("openid", wxPaymentContext.getOpenId());
        }

        String nonce_str = CommonUtil.getUUID().substring(0, 16);
        paraMap.put("nonce_str", nonce_str);
        paraMap.put("trade_type",wxPaymentContext.getTradeType());
        paraMap.put("notify_url", notifyUrl);
        String sign = WXUtil.createSign(paraMap, mchKey);
        paraMap.put("sign", sign);
        log.info("wxpreparesign:{}", JSON.toJSONString(paraMap));
        String xml = WXUtil.getRequestXml(paraMap);
        wxPaymentContext.setXml(xml);
    }

    @Override
    public Result process(PaymentContext context) {
        WXPaymentContext wxPaymentContext = (WXPaymentContext) context;
        log.info("WXPaymentparam=:{}", wxPaymentContext.getXml());
        String xml = HttpClientUtil.httpPost(UNIFIEDORDER_URL, wxPaymentContext.getXml());
        log.info("WXPaymentresult:{}", xml);
        Map<String, String> resultMap = WXUtil.doXMLParse(xml);
        if ("SUCCESS".equals(resultMap.get("result_code"))) {
            String prepayId = resultMap.get("prepay_id");
            String nonceStr = CommonUtil.getUUID();
            Long currentMills = System.currentTimeMillis();
            String timeStamp = currentMills.toString().substring(0, 10);
//            if(wxPaymentContext.getTradeType().equals("JSAPI")){
//                //H5支付，返回值改成
//                prepayId ="prepay_id=" + prepayId;
//            }

            SortedMap paraMap = new TreeMap<String, Object>();
            paraMap.put("appid", appId);
            paraMap.put("partnerid", mchId);
            paraMap.put("package", prepayId);
            //单位分
            paraMap.put("signType", wxPaymentContext.getSignType());
            paraMap.put("noncestr", nonceStr);

//            if(StringUtils.isNotBlank(wxPaymentContext.getOpenId())){
//                //微信h5支付必填
//                paraMap.put("openid", wxPaymentContext.getOpenId());
//            }

            paraMap.put("timestamp",timeStamp);
            String sign = WXUtil.createSign(paraMap, mchKey);
            log.info("wxprocesssign:{}", JSON.toJSONString(paraMap));

            WXPayDTO dto = new WXPayDTO(appId, mchId, prepayId, wxPaymentContext.getSignType(), nonceStr, timeStamp, sign);
            return new Result(dto);
        }
        String errMsg = resultMap.get("err_code") + ":" + resultMap.get("err_code_des");
        return ResultUtil.error(errMsg);
    }

    @Override
    public Result callback(CallBackParam param) {
        SortedMap<Object, Object> paraMap = new TreeMap<>();
        Map<String, String[]> resultMap = param.getResultMap();
        for (Iterator iter = resultMap.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next().toString();
            String value = Arrays.toString(resultMap.get(name));
            paraMap.put(name, value);
        }
        String orderNum = paraMap.get("out_trade_no").toString();

        //组装返回的结果的签名字符串
        String rsSign = resultMap.remove("sign").toString();
        String sign = WXUtil.createSign(paraMap, mchKey);
        log.info("wxpaycallback={},{}", orderNum, rsSign.equals(sign));
        //验证签名
        if (rsSign.equals(sign)) {
            if ("SUCCESS".equals(paraMap.get("result_code"))) {
                //TODO 更新交易日志表，交易成功

                //TODO 更新订单表状态已付款

                String data = WXUtil.setXML("SUCCESS", "OK");
                return new Result(data);
            }else {
                //TODO 更新交易日志表，交易失败

            }
        }
        return ResultUtil.error("fail");
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WECHAT_PAY.getCode();
    }

    @Override
    public Validator getValidator() {
        return wxPaymentValidator;
    }

    @Override
    public void afterProcess(PaymentContext context, Result result) {
        //TODO 生成交易日志表，交易中
    }

}
