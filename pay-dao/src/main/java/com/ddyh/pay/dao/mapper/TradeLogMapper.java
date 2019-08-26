package com.ddyh.pay.dao.mapper;

import com.ddyh.pay.dao.model.TradeLog;

public interface TradeLogMapper {
    int insert(TradeLog record);
    int update(TradeLog record);
    int updateTradeChannel(TradeLog record);
    TradeLog get(String tradeNo);
}