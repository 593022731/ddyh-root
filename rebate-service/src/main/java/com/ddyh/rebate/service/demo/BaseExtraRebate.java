package com.ddyh.rebate.service.demo;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 额外返利核心类，copy部分代码供参考
 */
@Slf4j
public abstract class BaseExtraRebate implements ExtraRebate {
    private static final String TYPE_REBATE_USER = "TYPE_REBATE_USER";
    private static final String TYPE_REBATE_CHANNEL = "TYPE_REBATE_CHANNEL";

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MemberTeamRepository memberTeamRepository;

    @Autowired
    private RebateRepository rebateRepository;

    @Autowired
    private TotalRebateRepository totalRebateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${constant.message.on}")
    private boolean isSendMsg;

    /**
     * // TODO 根据渠道类型，判断是否进行额外奖励分成，并根据订单类型判断是人头返利还是订单额外返利
     *
     * @param extraRebateTo
     */
    @Override
    public void rebate(ExtraRebateTo extraRebateTo) {
        Long uid = extraRebateTo.getUid();
        User user = userRepository.findUserByUid(uid);
        log.info("rebateuser={}", JSON.toJSONString(user));
        String parentFCode = user.getParentFcode();
        Long pid;
        if (parentFCode == null || "".equals(parentFCode)) {
            pid = 0L;
        } else {
            User parent = userRepository.findUserByFcode(parentFCode);
            log.info("rebateparentuser={}", JSON.toJSONString(parent));
            pid = parent.getUid();
        }
        // 若为团购类型返利，一级返给他自己
        if (extraRebateTo.getOrderType() == Constant.ORDER_TYPE_GROUP) {
            pid = uid;
        }
        /**
         * 若 pid 为0，进行额外返利存在两种情况
         *      1. 该节点为顶级用户购买
         *      2. 该用户为普通用户购买，没有绑定上下级关系
         *
         *     这两种情况都不进行额外返利，直接由渠道商接管
         */
        if (pid == 0) {
            return;
        }

        Rebate rebate = rebateRepository.findRebateByOrderIdAndOrderTypeAndRebateUserType(extraRebateTo.getOrderId(), extraRebateTo.getOrderType(), Constant.REBATE_USER_TYPE_CHANNEL);
        if (rebate == null) {
            log.info("rebatechannelisnull： {}， {}， {}", extraRebateTo.getUid(), extraRebateTo.getOrderId(), extraRebateTo.getOrderType());
            return;
        }
        Map<Short, Long> map = new HashMap<>();
        List<Short> identities = new ArrayList<>();
        // 迭代查找相邻上级
        while (pid != 0) {
            // 若已找到除顶级外的所有其他几级，则直接退出
            MemberTeam parentMemberTeam = memberTeamRepository.findMemberTeamByUserId(pid);
            log.info("rebateparentMemberTeam={}", parentMemberTeam);
            if (parentMemberTeam == null) {
                log.error("会员上级不存在于会员树中，额外返利出错，订单id为 [{}]，用户id为 [{}]", extraRebateTo.getOrderId(), uid);
                return;
            }
            Short identity = parentMemberTeam.getIdentity();
            pid = parentMemberTeam.getUserParentId();
            if (identity == 0) {
                continue;
            }
            // 若已经包含相同等级，则直接跳过
            if (map.containsKey(identity)) {
                continue;
            }
            // 过滤掉上级身份小于下级的情况
            boolean goon = true;
            for (Short s : identities) {
                if (s >= identity) {
                    goon = false;
                    break;
                }
            }
            if (!goon) {
                continue;
            }
            // 若返利查找已经达到顶级节点下一层，则总值查找
//            if (map.containsKey(IdentityConstant.CHIEF_INSPECTOR.getIdentity())) {
//                break;
//            }
            map.put(identity, parentMemberTeam.getUserId());
            identities.add(identity);
            // 若返利查找达到 “总裁”，则拒绝往上查找
            if (identity == IdentityConstant.CEO.getIdentity()) {
                break;
            }
        }

        // 计算返利
        List<RebateBo> rebateBos = countingRate(rebate, map);
        // 进行返利
        Short rebateType = extraRebateTo.getRebateType();
        // 延迟到账
        short accountEntryType = Constant.ACCOUNT_ENTRY_TYPE_LATER;
        if (rebateType == ExtraRebateType.GIFT.getType()) {
            // 立即到账
            accountEntryType = Constant.ACCOUNT_ENTRY_TYPE_IMMEDIATELY;
        }
        extraRebate(rebate, rebateBos, accountEntryType);
    }

    /**
     * 计算奖励金额，子类实现，大礼包与订单返利方式不同
     */
    protected abstract List<RebateBo> countingRate(Rebate rebate, Map<Short, Long> map);

    /**
     * 计算返利率
     *
     * @param map     上级返利集合
     * @param manager 默认经理返利率
     * @param chief   默认总监返利率
     * @param ceo     默认总裁返利率
     */
    Map<Short, BigDecimal> counting(Map<Short, Long> map, BigDecimal manager, BigDecimal chief, BigDecimal ceo) {
        if (map == null) {
            throw new RuntimeException("返利树为空");
        }
        BigDecimal zeroRate = new BigDecimal(0);
        boolean managerExist = map.containsKey(IdentityConstant.MANAGER.getIdentity());
        boolean chiefExist = map.containsKey(IdentityConstant.CHIEF_INSPECTOR.getIdentity());
        boolean ceoExist = map.containsKey(IdentityConstant.CEO.getIdentity());
        if (!managerExist && !chiefExist && !ceoExist) {
            // 1. 都不存在
            manager = chief = ceo = zeroRate;
        } else if (managerExist && !chiefExist && !ceoExist) {
            // 2. 只有经理
            chief = ceo = zeroRate;
        } else if (!managerExist && chiefExist && !ceoExist) {
            // 3. 只有总监
            chief = chief.add(manager);
            manager = ceo = zeroRate;
        } else if (!managerExist && !chiefExist) {
            // 4. 只有总裁
            ceo = ceo.add(chief).add(manager);
            manager = chief = zeroRate;
        } else if (managerExist && chiefExist && !ceoExist) {
            // 5. 经理，总监
            ceo = zeroRate;
        } else if (managerExist && !chiefExist) {
            // 6. 经理，总裁
            ceo = ceo.add(chief);
            chief = zeroRate;
        } else if (!managerExist) {
            // 7. 总监，总裁
            chief = chief.add(manager);
            manager = zeroRate;
        }

        // 身份倒挂有问题，需多加 10，删除倒挂记录
        // 只存在总监有问题，需多加 6
//        if (!map.containsKey(managerIdentity) && map.containsKey(chiefIdentity)) {
//            // 没有经理，有总监
//            chief = chief.add(ceo);
//            manager = zeroRate;
//        }else if (map.containsKey(managerIdentity) && !map.containsKey(chiefIdentity)){
//            ceo = ceo.add(chief);
//            chief = zeroRate;
//        }else if (!map.containsKey(managerIdentity) && !map.containsKey(chiefIdentity)){
//            ceo = ceo.add(chief).add(manager);
//            manager = zeroRate;
//            chief = zeroRate;
//        }
        Map<Short, BigDecimal> rateMap = new HashMap<>();
        rateMap.put(IdentityConstant.MANAGER.getIdentity(), manager);
        rateMap.put(IdentityConstant.CHIEF_INSPECTOR.getIdentity(), chief);
        rateMap.put(IdentityConstant.CEO.getIdentity(), ceo);

        return rateMap;
    }

    /**
     * 进行返利操作
     */
    private void extraRebate(Rebate rebate, List<RebateBo> list, Short accountEntryType) {
        User channelUser = userRepository.findByPhone(rebate.getRewardUserPhone());
        if (list == null || list.size() < 1) {
            // 发送渠道奖励短信
            sendRebateMsg(accountEntryType, channelUser, rebate.getRewardAmount(), rebate, TYPE_REBATE_CHANNEL);
            return;
        }
        // 根据订单id及返利用户类型（渠道商），查询对应订单渠道商返利
        // TODO 返利总数存在问题
        BigDecimal channelRewardAmount = rebate.getRewardAmount();
        int channelOrdinaryRebateNum = rebate.getRebateNum();
        int channelRebateNum = channelOrdinaryRebateNum;
        Date now = new Date();
        Date afterDate = TimeUtils.createAfterDate(1, now);
        if (accountEntryType == Constant.ACCOUNT_ENTRY_TYPE_IMMEDIATELY) {
            afterDate = now;
        }
        short rebateType = Constant.REBATE_TYPE_GIFT_EXTRA;
        if (rebate.getOrderType() == ExtraRebateType.ORDER.getType()) {
            rebateType = Constant.REBATE_TYPE_JD_EXTRA;
        }

        for (RebateBo rebateBo : list) {
            Rebate extraRebate = CommonUtils.dataHandler(rebate, new Rebate());
            extraRebate.setId(null);
            Long uid = rebateBo.getUid();
            extraRebate.setRewardUserId(uid);
            extraRebate.setCreateTime(now);
            extraRebate.setTimeToSettlement(afterDate);
            extraRebate.setRebateUserType(Constant.REBATE_USER_TYPE_MEMBER);
            extraRebate.setRebateType(rebateType);
            User rewardUser = userRepository.findOne(uid);
            extraRebate.setRewardUserPhone(rewardUser.getPhone());

            Integer rebateNum = rebateBo.getRebateNum();
            log.info("订单id [{}]，用户id [{}]，额外返利数为 [{}]", extraRebate.getOrderId(), uid, rebateNum);
            if (rebateNum < 1) {
                log.info("订单额外返利太少，订单id [{}]，用户id [{}]，额外返利数为 [{}]", extraRebate.getOrderId(), uid, rebateNum);
                continue;
            }
            // 计算返利金额
            BigDecimal extraRewardAmount = new BigDecimal(rebateNum).movePointLeft(2);
            channelRewardAmount = channelRewardAmount.subtract(extraRewardAmount);
            extraRebate.setRewardAmount(extraRewardAmount);      // 修改奖励额度
            extraRebate.setRebateNum(rebateNum);     // 修改返利数
            channelRebateNum -= rebateNum;

            rebateRepository.save(extraRebate);
            // 修改总返利表
            updateTotalRebate(uid, rebateNum, now, accountEntryType, Constant.REBATE_USER_TYPE_MEMBER, rewardUser.getPhone());
            // 额外奖励短信推送
            sendRebateMsg(accountEntryType, rewardUser, extraRewardAmount, rebate, TYPE_REBATE_USER);
        }
        log.info("RebateBo list: {}", JSON.toJSONString(list));
        rebate.setRewardAmount(channelRewardAmount);
        rebate.setRebateNum(channelRebateNum);
        rebateRepository.save(rebate);
        // 修改总返利表
        updateTotalRebate(rebate.getRewardUserId(), (channelRebateNum - channelOrdinaryRebateNum), now, accountEntryType, Constant.REBATE_USER_TYPE_CHANNEL, rebate.getRewardUserPhone());
        // 渠道商奖励短信推送
        sendRebateMsg(accountEntryType, channelUser, channelRewardAmount, rebate, TYPE_REBATE_CHANNEL);
    }

    private void sendRebateMsg(short accountEntryType, User rewardUser, BigDecimal rewardAmount, Rebate rebate, String rebateType) {
        if (!isSendMsg) {
            return;
        }
        try {
            log.info("sendRebateMsg isSendMsg: {} accountEntryType: {} phone: {}", isSendMsg, accountEntryType, rewardUser.getPhone());
            // 小于1块不发短信
            if (rewardAmount.compareTo(new BigDecimal(1)) < 0) {
                return;
            }
//            String testPhone = "18356508285";
//            String sendPhone = "15067187119";
            String sendPhone = rewardUser.getPhone();
            User buyUser = userRepository.findOne(rebate.getConsumerId());
            Order order;
            OrderItem orderItem;
            boolean isGift = accountEntryType == Constant.ACCOUNT_ENTRY_TYPE_IMMEDIATELY;
            boolean isPreBuy = Constant.ORDER_TYPE_GROUP == rebate.getOrderType();
            boolean isChannelRebate = TYPE_REBATE_CHANNEL.equals(rebateType);
            String productName = "快速晋升通道";

            if (!isPreBuy) {
                order = orderRepository.findOne(rebate.getOrderId());
                if (order == null) {
                    log.error("sendRebateMsg error order is null");
                    return;
                }
                orderItem = order.getOrderItems().get(0);
                if (orderItem == null) {
                    log.error("sendRebateMsg error orderItem is null");
                    return;
                }
                String itemName = orderItem.getProductName();
                if (StringUtils.isNotEmpty(itemName) && itemName.length() > 20) {
                    productName = itemName.substring(0, 20) + "..";
                } else {
                    productName = "";
                }
            }

            Long consumerId = rebate.getConsumerId();
            User consumerUser = userRepository.findOne(consumerId);
            String parentFCode = consumerUser.getParentFcode();
            if (TYPE_REBATE_USER.equals(rebateType) && StringUtils.isBlank(parentFCode)) {
                return;
            }

            // 大礼包
            short level = 0;
            if (isGift) {
                if (!isChannelRebate && !isPreBuy) {
                    log.info("send user rebate...");
                    // 如果不是渠道和团购 区分二级和其他身份
                    User parent = userRepository.findUserByFcode(parentFCode);
                    Long parentId = parent.getUid();
                    log.info("parentId: {}", parentId);
                    log.info("rewardUserId: {}", rewardUser.getUid());

                    if (parentId.equals(rewardUser.getUid())) {
                        // 父级奖励
                        level = Constant.REBATE_TYPE_GIFT_EXTRA_FIRST_LEVEL;
                    }
                }

                // 团购大礼包 写死商品名字 使用其他身份模板
                if (isPreBuy) {
                    SMSUtil.sendPreBuyGiftNotify(sendPhone, buyUser.getNickName(), buyUser.getPhone(), productName, rewardAmount, level);
                    return;
                }
                SMSUtil.sendGiftNotify(sendPhone, buyUser.getNickName(), buyUser.getPhone(), productName, rewardAmount, level);
                return;
            }

            // 京东商品
            SMSUtil.sendJdNotify(sendPhone, buyUser.getNickName(), buyUser.getPhone(), productName, rewardAmount);
        } catch (Exception e) {
            log.error("sendRebateMsg error: {}", e);
        }

    }

    /**
     * 增加返利时，修改总返利表
     *
     * @param uid              返利用户id
     * @param rebateNum        新增返利数
     * @param lastTime         上次返利时间
     * @param accountEntryType 是否入账，0 为未入账（代表正常购买的商品），1 为未审核（代表已入账，大礼包产品）
     * @param rebateUserType   返利用户类型，1 为 会员/分销，2 为渠道
     */
    private void updateTotalRebate(Long uid, Integer rebateNum, Date lastTime, short accountEntryType, short rebateUserType, String rewardUserPhone) {
        // 在总返利上增加返利数目
        TotalRebate totalRebate = totalRebateRepository.findTotalRebateByRebateUserIdAndRebateUserType(uid, rebateUserType);
        if (totalRebate == null) {
            totalRebate = new TotalRebate(uid, 0, 0, 0, 0,
                    lastTime, rebateUserType, rewardUserPhone);
        }
        // 根据入账类型，操作总返利数
        if (accountEntryType == Constant.ACCOUNT_ENTRY_TYPE_LATER) {
            totalRebate.setUnAccountEntryTotalNum(totalRebate.getUnAccountEntryTotalNum() + rebateNum);
        } else {
            totalRebate.setAccountEntryTotalNum(totalRebate.getAccountEntryTotalNum() + rebateNum);
        }
        totalRebateRepository.save(totalRebate);
    }

}
