package com.ddyh.pay.service.services;

import com.ddyh.pay.dao.mapper.TradeLogMapper;
import com.ddyh.pay.dao.model.TradeLog;
import com.ddyh.pay.facade.constant.PayChannelEnum;
import com.ddyh.pay.facade.constant.TradeStatusEnum;
import com.ddyh.pay.facade.constant.TradeTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author: weihui
 * @Date: 2019/8/22 16:39
 */
@Service
public class TradeLogService {
    @Resource
    private TradeLogMapper tradeLogMapper;

    /**
     * 新增交易记录
     *
     * @param tradeNo
     * @param tradeAmount
     * @param payChannel
     * @param tradeType
     */
    public void save(String tradeNo, BigDecimal tradeAmount, PayChannelEnum payChannel, TradeTypeEnum tradeType) {
        TradeLog item = new TradeLog();
        item.setTradeNo(tradeNo);
        item.setTradeAmount(tradeAmount);
        item.setTradeChannel(payChannel.getCode());
        item.setTradeType(tradeType.getCode());
        item.setTradeStatus(TradeStatusEnum.COMMIT.getCode());
        tradeLogMapper.insert(item);
    }

    /**
     * 更新交易记录
     *
     * @param tradeNo
     * @param outTradeNo
     * @param tradeSuccessTime
     * @param tradeStatus
     * @param remark
     */
    public void update(String tradeNo, String outTradeNo, String tradeSuccessTime, TradeStatusEnum tradeStatus, String remark) {
        TradeLog item = new TradeLog();
        item.setTradeNo(tradeNo);
        item.setOutTradeNo(outTradeNo);
        item.setTradeStatus(tradeStatus.getCode());
        item.setRemark(remark);
        item.setTradeSuccessTime(tradeSuccessTime);
        tradeLogMapper.update(item);
    }

    /**
     * 查询详情
     * @param tradeNo
     * @return
     */
    public TradeLog get(String tradeNo){
        return tradeLogMapper.get(tradeNo);
    }
}
