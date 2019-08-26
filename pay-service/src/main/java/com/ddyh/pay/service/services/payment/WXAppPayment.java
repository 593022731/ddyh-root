package com.ddyh.pay.service.services.payment;


import com.alibaba.fastjson.JSON;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.utils.CommonUtil;
import com.ddyh.commons.utils.HttpClientUtil;
import com.ddyh.commons.utils.ResultUtil;
import com.ddyh.pay.dao.model.TradeLog;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.constant.TradeStatusEnum;
import com.ddyh.pay.facade.constant.TradeTypeEnum;
import com.ddyh.pay.facade.dto.WXAppPayDTO;
import com.ddyh.pay.facade.param.CallBackParam;
import com.ddyh.pay.facade.param.RequestParam;
import com.ddyh.pay.facade.param.WXPayParam;
import com.ddyh.pay.service.services.TradeLogService;
import com.ddyh.pay.service.services.context.PaymentContext;
import com.ddyh.pay.service.services.context.WXPaymentContext;
import com.ddyh.pay.service.services.core.BasePayCoreService;
import com.ddyh.pay.service.services.validator.Validator;
import com.ddyh.pay.service.services.validator.WXAppPaymentValidator;
import com.ddyh.pay.service.util.WXUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信APP支付
 *
 * @author: weihui
 * @Date: 2019/8/19 16:17
 */
@Service("wxAppPayment")
public class WXAppPayment extends BasePayCoreService {

    /**
     * 统一下单请求URL
     */
    protected static final String UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     * 应用id
     */
    @Value("${wx.app.pay.appId}")
    private String appId;
    /**
     * 商户号
     */
    @Value("${wx.app.pay.mchId}")
    private String mchId;
    /**
     * 商户密钥key
     */
    @Value("${wx.app.pay.mchKey}")
    protected String mchKey;

    /**
     * 异步回调地址
     */
    @Value("${wx.app.pay.notifyUrl}")
    private String notifyUrl;

    @Resource(type = WXAppPaymentValidator.class)
    private WXAppPaymentValidator wxPaymentValidator;

    @Resource
    protected TradeLogService tradeLogService;

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
        wxPaymentContext.setBody(wxParam.getBody());
        return wxPaymentContext;
    }

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

        String nonce_str = CommonUtil.getUUID().substring(0, 16);
        paraMap.put("nonce_str", nonce_str);
        paraMap.put("trade_type",wxPaymentContext.getTradeType());
        paraMap.put("notify_url", notifyUrl);
        //调用统一下单前的签名，字段名看文档：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
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
            //32位长度
            String nonceStr = CommonUtil.getUUID();
            Long currentMills = System.currentTimeMillis();
            //10位长度
            String timeStamp = currentMills.toString().substring(0, 10);

            SortedMap paraMap = new TreeMap<String, Object>();
            paraMap.put("appid", appId);
            paraMap.put("partnerid", mchId);
            paraMap.put("prepayid", prepayId);
            paraMap.put("package", wxPaymentContext.getSignType());
            paraMap.put("noncestr", nonceStr);

            paraMap.put("timestamp",timeStamp);
            //调用微信唤起的时候，要重新签名，注意和统一下单的参数名完全不一样，字段名看文档：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2
            String sign = WXUtil.createSign(paraMap, mchKey);
            log.info("wxprocesssign:{}", JSON.toJSONString(paraMap));

            WXAppPayDTO dto = new WXAppPayDTO(appId, mchId, prepayId, wxPaymentContext.getSignType(), nonceStr, timeStamp, sign);
            return new Result(dto);
        }
        String errMsg = resultMap.get("err_code") + ":" + resultMap.get("err_code_des");
        return ResultUtil.error(errMsg);
    }

    @Override
    public Result callback(CallBackParam param) {
        String wxParam = param.getWxParam();

        try {
            SortedMap<Object, Object> params = new TreeMap();
            Document doc = DocumentHelper.parseText(wxParam);
            Element root = doc.getRootElement();
            for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
                Element e = (Element) iterator.next();
                params.put(e.getName(), e.getText());
            }
            log.info("wxpaycallbackresult={}", JSON.toJSONString(params));
            //组装返回的结果的签名字符串
            String rsSign = params.remove("sign").toString();
            String sign = WXUtil.createSign(params, mchKey);
            String orderNum = params.get("out_trade_no").toString();

            log.info("wxapppaycallback={},{}", orderNum, rsSign.equals(sign));
            //验证签名
            if (rsSign.equals(sign)) {
                //已回调，直接return
                TradeLog tradeLog = tradeLogService.get(orderNum);
                if(!tradeLog.getTradeStatus().equals(TradeStatusEnum.COMMIT.getCode())){
                    throw new BusinessException("重复提交");
                }

                if ("SUCCESS".equals(params.get("result_code"))) {
                    //更新交易日志表，交易成功
                    String tradeSuccessTime = params.get("time_end").toString();//"time_end":"20190826154153"
                    try {
                        tradeSuccessTime = DateFormatUtils.format(DateUtils.parseDate(tradeSuccessTime,"yyyyMMddHHmmss"),"yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    tradeLogService.update(orderNum,params.get("transaction_id").toString(),tradeSuccessTime, TradeStatusEnum.FINISH,"");

                    //TODO 更新订单表状态已付款

                }else {
                    //更新交易日志表，交易失败
                    tradeLogService.update(orderNum,params.get("transaction_id").toString(), DateFormatUtils.format(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"), TradeStatusEnum.FAIL,"");
                }
                //返回给微信参数，让其不在继续回调
                return new Result(orderNum);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return ResultUtil.error("fail");
    }

    @Override
    public String getPayChannel() {
        return PayChannelEnum.WX_APP_PAY.getCode();
    }

    @Override
    public Validator getValidator() {
        return wxPaymentValidator;
    }

    @Override
    public void afterProcess(PaymentContext context, Result result) {
        //生成交易日志表，交易中
        WXPaymentContext paymentContext = (WXPaymentContext) context;
        String tradeNo = paymentContext.getOutTradeNo();
        BigDecimal totalFee = new BigDecimal(paymentContext.getTotalFee()).movePointLeft(2);
        tradeLogService.save(tradeNo,totalFee,PayChannelEnum.WX_APP_PAY, TradeTypeEnum.PAY);
    }

}
