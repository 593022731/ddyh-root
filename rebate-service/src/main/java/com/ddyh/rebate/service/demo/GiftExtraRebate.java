package com.ddyh.rebate.service.demo;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * 大礼包额外返利
 */
@Component
@Slf4j
public class GiftExtraRebate extends BaseExtraRebate {

    /**
     * 计算返利
     */
    @Override
    protected List<RebateBo> countingRate(Rebate rebate, Map<Short, Long> map){
        List<RebateBo> list = new ArrayList<>();
        Map<Short, BigDecimal> rateMap = counting(map, Constant.MANAGER_AMOUNT, Constant.CHIEF_AMOUNT, Constant.CEO_AMOUNT);
        Set<Short> set = map.keySet();
        BigDecimal channelRebate = rebate.getRewardAmount();
        for (Short s : set) {
            Long uid = map.get(s);
            BigDecimal rate = rateMap.get(s);
            if (rate != null) {
//                list.add(new RebateBo(uid, rate.movePointRight(2).intValue()));
                BigDecimal res = rate.multiply(channelRebate);
                res = roundGiftRebate(res.setScale(0, BigDecimal.ROUND_HALF_UP));
                log.info("用户id [{}]， 身份为 [{}]， 返利 [{}]", uid, s, res);
                list.add(new RebateBo(uid, res.movePointRight(2).intValue()));
            }
        }
        return list;
    }

    private BigDecimal roundGiftRebate(BigDecimal rebate) {
        if (rebate.intValue() % 5 == 0) {
            return rebate;
        }
        BigDecimal temp;
        for (int i = 1 ; i < 5 ; i ++) {
            BigDecimal count = new BigDecimal(i);
            temp = rebate.add(count);
            if (temp.intValue() % 5 == 0) {
                return temp;
            }
            temp = rebate.subtract(count);
            if (temp.intValue() % 5 == 0) {
                return temp;
            }
        }
        // 计算出错
        return rebate;
    }
}
