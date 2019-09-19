package com.ddyh.rebate.service.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * 京东订单额外返利
 */
@Component
public class OrderExtraRebate extends BaseExtraRebate {

    /**
     * 计算返利
     */
    protected List<RebateBo> countingRate(Rebate rebate, Map<Short, Long> map) {
        List<RebateBo> list = new ArrayList<>();
        Map<Short, BigDecimal> rateMap = counting(map, Constant.MANAGER_RATE, Constant.CHIEF_RATE, Constant.CEO_RATE);
        Set<Short> set = map.keySet();
        // 总利润的百分比计算，通过金钻数计算
        BigDecimal channelRebate = rebate.getOrderProfit().movePointRight(2);
        for (Short s : set) {
            Long uid = map.get(s);
            BigDecimal rate = rateMap.get(s);
            // 通过比例计算价格，计算返利金额
            if (rate != null) {
                list.add(new RebateBo(uid, rate.multiply(channelRebate).intValue()));
            }
        }
        return list;
    }
}
