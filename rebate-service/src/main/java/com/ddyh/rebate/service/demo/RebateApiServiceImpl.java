package com.ddyh.rebate.service.demo;


import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (部分代码)A系统返利兑换及相关查询逻辑
 */
@Slf4j
@Service
public class RebateApiServiceImpl implements RebateApiService {

    private final IdentityServer identityServer;

    private final TotalRebateApiRepository totalRebateApiRepository;

    private final RebateApiRepository rebateApiRepository;

    private final RebateAlreadyApiRepository rebateAlreadyApiRepository;

    private final DistributorRepository distributorRepository;
    private final ExchanglogRepository exchanglogRepository;
    private DictionaryRepository dictionaryRepository;
    private RedisUtils redisUtils;

    @Autowired
    public RebateApiServiceImpl(IdentityServer identityServer, TotalRebateApiRepository totalRebateApiRepository, RebateApiRepository rebateApiRepository, RebateAlreadyApiRepository rebateAlreadyApiRepository, DistributorRepository distributorRepository, ExchanglogRepository exchanglogRepository
            , DictionaryRepository dictionaryRepository, RedisUtils redisUtils) {
        this.identityServer = identityServer;
        this.totalRebateApiRepository = totalRebateApiRepository;
        this.rebateApiRepository = rebateApiRepository;
        this.rebateAlreadyApiRepository = rebateAlreadyApiRepository;
        this.distributorRepository = distributorRepository;
        this.exchanglogRepository = exchanglogRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.redisUtils = redisUtils;
    }

    @Value("${break.extra.rebate.start.time}")
    private String extraRebateStartTime;

    @Value("${constant.test.data}")
    private boolean testData;

    @Override
    public ServerAndExchangeInfoDto getServerAndExchangeInfo() {
        Date now = new Date();
        if (testData) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.set(Calendar.DAY_OF_MONTH, 10);//每月10号可以兑换
            now = calendar.getTime();
        }

        HttpServletRequest request = RequestContextHolderUtil.getRequest();
        String currentVersion = request.getHeader("currentVersion");
        String appType = request.getHeader("appType");
        if ("1.2.3".equals(currentVersion) && "2".equals(appType)) {
            //紧急修复IOS bug，后面强制升级版本可以删掉这块代码
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.set(Calendar.DAY_OF_MONTH, 10);
            now = calendar.getTime();
        }

        ServerAndExchangeInfoDto serverAndExchangeInfoDto = new ServerAndExchangeInfoDto();
        serverAndExchangeInfoDto.setServerTime(now);
        User currentUser = identityServer.getCurrentUser();
        Long uid = currentUser.getUid();
        Distributor distributor = distributorRepository.findDistributorByUserId(uid);
        if (distributor == null) {
            serverAndExchangeInfoDto.setExchange(-1);
            return serverAndExchangeInfoDto;
        }
        Short reviewState = distributor.getReviewState();
        if (reviewState == Constant.DISTRIBUTOR_PASS_REVIEW) {
            // 审核通过
            serverAndExchangeInfoDto.setExchange(1);
        } else {
            serverAndExchangeInfoDto.setExchange(0);
        }
        // 代码写死已签约，以后会加上
        distributor.setContractState((short) 1);
        serverAndExchangeInfoDto.setDistributorDto(CommonUtil.dataHandler(distributor, new DistributorDto()));

        return serverAndExchangeInfoDto;
    }

    @Override
    public TotalRebate getTotalRebate() {
        User currentUser = identityServer.getCurrentUser();
        return totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void applyRebate(ApplyRebateVo applyRebateVo) {
        int applyCount = applyRebateVo.getCount();
        // 兑换额度限定，1000以上，并以1000为增长
        if (applyCount < 1000 || applyCount % 1000 > 0) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        User currentUser = checkUserLegal();
        // 加锁 防止点击过快
        boolean lock = redisUtils.getLock(RedisConstant.A_B_SYSTEM_REBATE_LOCK_KEY_PREFIX + currentUser.getUid(), "1", RedisConstant.A_B_SYSTEM_REBATE_LOCK_KEY_EXPIRE);
        if (!lock) {
            throw new BusinessException("系统处理中，请稍后重试!");
        }
        // 兑换申请增加额度限制 单人每月最多2.8w
        List<Dictionary> dataList = dictionaryRepository.findByDictValueAndIsUsed(Constant.REBATE_APPLY_AMOUNT_LIMIT, Constant.DICT_IN_USE);
        if (CollectionUtils.isEmpty(dataList)) {
            throw new BusinessException(ResultCode.DICT_DATA_NOT_FOUND);
        }
        Dictionary amountLimit = dataList.get(0);
        if (applyCount >= Integer.valueOf(amountLimit.getDataValue())) {
            throw new BusinessException(ResultCode.REBATE_APPLY_OUT_NUMBER);
        }
        isOutNumberLimitAmount(currentUser.getUid(), amountLimit, applyRebateVo.getCount());

        // 1. 查询未复核数
        TotalRebate totalRebate = totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER);
        if (totalRebate == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        // 2. 查询所有未复核单
        List<Rebate> list = rebateApiRepository.findRebatesByRewardUserIdAndStateAndRebateUserType(currentUser.getUid(), Constant.REBATE_STATE_UNCHECKED, Constant.USER_REBATE_TYPE_MEMBER);
        int unCheckNum = 0;
        if (list != null && list.size() > 0) {
            for (Rebate rebate : list) {
                unCheckNum += rebate.getRebateNum();
                rebate.setState(Constant.REBATE_STATE_CHECKED);
                rebateApiRepository.save(rebate);
            }
        }
        // 3. 比较未复核数与所有未复核单总数
        int num = totalRebate.getAccountEntryTotalNum() - totalRebate.getCheckNum();
        if (num != unCheckNum) {
            throw new BusinessException(ResultCode.REPEAT_CHECK_ERROR);
        }
        // 4. 计算用户剩余可兑换金钻数
        int surplusNum = getSurplusNum(false);
        // 5. 计算差值与申请返利数之间的关系
        if (applyCount > surplusNum) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        // 6. 更新复核数
        totalRebate.setCheckNum(totalRebate.getCheckNum() + num);
        totalRebate.setLastRebateTime(new Date());
        if (totalRebate.getAccountEntryTotalNum().equals(totalRebate.getRebatedNum())) {
            // b系统已将分销商余额兑完， 扣除联创总表余额(已兑换累加)
            TotalRebate channelTotalRebate = totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(currentUser.getChannelId(),
                    Constant.USER_REBATE_TYPE_CHANNEL);
            if (channelTotalRebate == null) {
                log.error("applyRebate error no channelTotalRebate uid: {}", currentUser.getUid());
                throw new BusinessException("兑换异常, 无渠道信息");
            }
            channelTotalRebate.setRebatedNum(channelTotalRebate.getRebatedNum() + applyCount);
            totalRebateApiRepository.save(channelTotalRebate);
        } else {
            totalRebate.setRebatedNum(totalRebate.getRebatedNum() + applyCount);
        }
        totalRebateApiRepository.save(totalRebate);
        // 7. 新建已返利数据
        RebateAlready rebateAlready = new RebateAlready(currentUser.getUid(), applyCount, new Date(),
                Constant.APPLY_REBATE_STATE_COMMIT, Constant.USER_REBATE_TYPE_MEMBER, Constant.REBATE_ALREADY_FROM_A, currentUser.getPhone());
        rebateAlreadyApiRepository.save(rebateAlready);
    }


    private void isOutNumberLimitAmount(long uid, Dictionary amountLimit, int applyCount) {
        Distributor distributor = distributorRepository.findDistributorByUserId(uid);
        if (distributor == null || StringUtils.isEmpty(distributor.getIdCard())) {
            throw new BusinessException("分销商不存在或身份证号不全!");
        }
        List<User> sameIdCard = distributorRepository.findUserPhoneByIdCard(distributor.getIdCard());
        Set<String> destUserPhone = sameIdCard.stream().map(User::getPhone).collect(Collectors.toSet());
        Date now = new Date();
        // 已返利状态
        Set<Short> rebateAlreadyState = new HashSet<>();
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_COMMIT);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_CHECKED);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_EXCHANGE);
        Date thisMonthStartTime = DateUtil.getDateMonthStartTime(now);
        Date thisMonthEndTime = DateUtil.getDateMonthEndTime(now);
        int rebateAlreadyTotalNum = rebateAlreadyApiRepository.getRebateAlreadyTotalNumByRebateTime(destUserPhone, rebateAlreadyState,
                thisMonthStartTime, thisMonthEndTime);

        if (rebateAlreadyTotalNum + applyCount >= Integer.valueOf(amountLimit.getDataValue())) {
            throw new BusinessException(ResultCode.REBATE_APPLY_OUT_NUMBER);
        }
    }

    public static void main(String[] args) {
        Date thisMonthStartTime = DateUtil.getDateMonthStartTime(new Date());
        Date thisMonthEndTime = DateUtil.getDateMonthEndTime(new Date());
        System.out.println(thisMonthStartTime);
        System.out.println(thisMonthEndTime);
    }

    @Override
    public ExchangeTimesAndQuotaDto getExchangeTimesAndQuota() {
        User currentUser = identityServer.getCurrentUser();
        Long uid = currentUser.getUid();
        return rebateAlreadyApiRepository.getExchangeTimesAndQuota(uid, Constant.APPLY_REBATE_STATE_EXCHANGE,
                Constant.REBATE_ALREADY_FROM_A);
    }

    @Override
    public PageResult<RebateAlready> getRebateAlreadyList(BasePageVo basePageVo) {
        User currentUser = checkUserLegal();
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(basePageVo.getCurrentPage() - 1, basePageVo.getPageSize(), sort);
        Page<RebateAlready> page = rebateAlreadyApiRepository.findAllByRebateUserIdAndRebateFrom(currentUser.getUid(),
                Constant.REBATE_ALREADY_FROM_A, pageable);
        List<RebateAlready> content = page.getContent();
        long total = page.getTotalElements();
        return new PageResult<>(total, content);
    }

    @Override
    public PageResult<Rebate> getRebateList(BasePageVo basePageVo) {
        User currentUser = checkUserLegal();
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(basePageVo.getCurrentPage() - 1, basePageVo.getPageSize(), sort);
        // 查询的返利状态
        Set<Short> rebateState = new HashSet<>();
        rebateState.add(Constant.REBATE_STATE_UNCHECKED);
        rebateState.add(Constant.REBATE_STATE_CHECKED);
        Set<Short> rebateType = new HashSet<>();
        // 返利分割后查询的返利类型
        rebateType.add(Constant.REBATE_TYPE_GIFT_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_GIFT_SECOND_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_SECOND_LEVEL);
        //新加的三种返利
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD);
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD_JD);
        rebateType.add(Constant.REBATE_TYPE_SHARE_PROFIT);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime;
        try {
            startTime = sdf.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.REMOTE_ACCESS_ERROR);
        }
        Page<Rebate> page = rebateApiRepository.getTwoLevelRebateList(currentUser.getUid(), rebateState,
                Constant.USER_REBATE_TYPE_MEMBER, startTime, rebateType, pageable);
        List<Rebate> content = page.getContent();
        long total = page.getTotalElements();
        return new PageResult<>(total, content);
    }

    @Override
    public CountingRebateByDateDto getRebateNumByDate(Date beforeTime, Date afterTime) {
        User currentUser = identityServer.getCurrentUser();
        // 1. 计算返利拆分日期之前所有的返利单子数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date extraStartTime;
        try {
            extraStartTime = sdf.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.REMOTE_ACCESS_ERROR);
        }
        // 返利类型
        Set<Short> rebateType = new HashSet<>();
        rebateType.add(Constant.REBATE_TYPE_GIFT_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_GIFT_SECOND_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_SECOND_LEVEL);

        //新加的三种返利
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD);
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD_JD);
        rebateType.add(Constant.REBATE_TYPE_SHARE_PROFIT);
        CountingRebateByDateDto countingRebateByDateDto;
        if (extraStartTime.compareTo(beforeTime) > 0 && extraStartTime.compareTo(afterTime) < 0) {
            // 获取两段
            CountingRebateByDateDto firstCount = rebateApiRepository.getCountRebateByDate(beforeTime, extraStartTime, currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, rebateType, false);
            CountingRebateByDateDto secondCount = rebateApiRepository.getCountRebateByDate(extraStartTime, afterTime, currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, rebateType, true);
            if (firstCount == null) {
                return new CountingRebateByDateDto(0L, 0L);
            }
            countingRebateByDateDto = new CountingRebateByDateDto(firstCount.getOrderCount() + secondCount.getOrderCount(), firstCount.getRebateNum() + secondCount.getRebateNum());
        } else if (extraStartTime.compareTo(afterTime) > 0) {
            countingRebateByDateDto = rebateApiRepository.getCountRebateByDate(beforeTime, afterTime, currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, rebateType, false);
        } else {
            countingRebateByDateDto = rebateApiRepository.getCountRebateByDate(beforeTime, afterTime, currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, rebateType, true);
        }
        return countingRebateByDateDto;
    }

    @Override
    public PageResult<Rebate> getRebateInfoByDate(Date beforeTime, Date afterTime, BasePageVo basePageVo) {
        User currentUser = identityServer.getCurrentUser();
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(basePageVo.getCurrentPage() - 1, basePageVo.getPageSize(), sort);
        // 返利类型
        Set<Short> rebateType = new HashSet<>();
        rebateType.add(Constant.REBATE_TYPE_GIFT_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_GIFT_SECOND_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_SECOND_LEVEL);

        //新加的三种返利
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD);
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD_JD);
        rebateType.add(Constant.REBATE_TYPE_SHARE_PROFIT);

        Page<Rebate> page = rebateApiRepository.getRebateListByDate(beforeTime, afterTime, rebateType,
                Constant.REBATE_STATE_CANCEL, currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, pageable);
        List<Rebate> content = page.getContent();
        long total = page.getTotalElements();
        return new PageResult<>(total, content);
    }

    /**
     * 计算用户剩余可兑换金钻数
     *
     * @param all 是否包含未入账数，true 包含，false 不包含
     */
    @Override
    public int getSurplusNum(boolean all) {
        User currentUser = identityServer.getCurrentUser();
        Long uid = currentUser.getUid();
        // 1. 计算返利拆分日期之前所有的返利单子数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date extraStartTime;
        try {
            extraStartTime = sdf.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.REMOTE_ACCESS_ERROR);
        }
        // 返利状态
        Set<Short> rebateState = new HashSet<>();
        if (all) {
            rebateState.add(Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY);
        }
        rebateState.add(Constant.REBATE_STATE_UNCHECKED);
        rebateState.add(Constant.REBATE_STATE_CHECKED);
        int splitRebateBeforeNum = rebateApiRepository.getSplitRebateBefore(uid, extraStartTime, rebateState,
                Constant.USER_REBATE_TYPE_MEMBER);
        // 2. 计算返利拆分日期之后所有的二级返利单子数
        // 返利类型
        Set<Short> rebateType = new HashSet<>();
        rebateType.add(Constant.REBATE_TYPE_GIFT_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_GIFT_SECOND_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_FIRST_LEVEL);
        rebateType.add(Constant.REBATE_TYPE_JD_SECOND_LEVEL);

        //新加的三种返利
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD);
        rebateType.add(Constant.REBATE_TYPE_EXP_CARD_JD);
        rebateType.add(Constant.REBATE_TYPE_SHARE_PROFIT);
        int splitRebateAfterNum = rebateApiRepository.getSplitRebateAfter(uid, extraStartTime, rebateType,
                rebateState, Constant.USER_REBATE_TYPE_MEMBER);
        // 3. 计算所有的已返利二级单子数
        // 已返利状态
        Set<Short> rebateAlreadyState = new HashSet<>();
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_COMMIT);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_CHECKED);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_EXCHANGE);
        int rebateAlreadyTotalNum = rebateAlreadyApiRepository.getRebateAlreadyTotalNum(uid, rebateAlreadyState,
                Constant.USER_REBATE_TYPE_MEMBER, Constant.REBATE_ALREADY_FROM_A);
        return splitRebateBeforeNum + splitRebateAfterNum - rebateAlreadyTotalNum;
    }

    @Transactional
    @Override
    public String meiChaicallback(String tradeNo, String outTradeNo, String tradeSuccessTime, String tradeAmount) {
        Exchanglog exchanglog = exchanglogRepository.findExchanglogByTradeNo(tradeNo);
        if (exchanglog == null) {
            log.error("meiChaicallbackerror,noexchanglog");
            return "fail";
        }
        if (exchanglog.getTradeStatus() == ExchangTradeState.SUCCESS.getState()) {
            //已打款成功
            return "success";
        }

        if (!exchanglog.getTradeAmount().equals(tradeAmount)) {
            log.error("meiChaicallbackerror,tradeAmounterror:" + exchanglog.getTradeAmount() + "," + tradeAmount);
            return "fail";
        }

        RebateAlready rebateAlready = rebateAlreadyApiRepository.findOne(Long.valueOf(tradeNo));
        if (rebateAlready == null) {
            log.error("meiChaicallbackerror,norebateAlready");
            return "fail";
        }

        //打款成功，更新流水记录表和返利状态
        exchanglogRepository.updateTrade(exchanglog.getId(), ExchangTradeState.SUCCESS.getState(), outTradeNo, tradeSuccessTime);

        //回调成功后修改返利状态
        rebateAlready.setState(AlreadyRebateState.APPLY_REBATE_STATE_EXCHANGE.getState());
        rebateAlready.setAccountNum(tradeNo);
        rebateAlready.setAccountTime(tradeSuccessTime);
        rebateAlready.setWxAccountNum(outTradeNo);
        rebateAlreadyApiRepository.save(rebateAlready);
        return "success";
    }

    /**
     * 校验用户合法性
     *
     * @return
     */
    private User checkUserLegal() {
        User currentUser = identityServer.getCurrentUser();
        Short characterType = currentUser.getCharacterType();
        if (characterType < Constant.USER_MEMBER_TYPE) {
            // 普通人不可查看
            throw new BusinessException("普通人不可查看");
        }
        return currentUser;
    }

    @Override
    public void saveRebate(Long orderId, Long uid, Long consumerId, String consumerPhone, BigDecimal rewardAmount, Short state, Short orderType, BigDecimal orderNakedPrice, Date timeToSettlement, Short rebateUserType, Integer rebateNum,
                           Short rebateType, BigDecimal orderProfit, String rewardUserPhone) {
        Rebate rebate = new Rebate(orderId, consumerId, uid, rewardAmount, state, new Date(), consumerPhone, orderType, orderNakedPrice, timeToSettlement, rebateUserType, rebateNum, rebateType, orderProfit, rewardUserPhone);
        rebateApiRepository.saveAndFlush(rebate);

        // 在总返利上增加返利数目
        // FIXME 加上修改版本， 乐观锁
        TotalRebate totalRebate = totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(uid, rebateUserType);
        if (totalRebate == null) {
            totalRebate = new TotalRebate(uid, 0, 0, 0, 0,
                    new Date(), rebateUserType, rewardUserPhone);
        }
        // 根据入账类型，操作总返利数
        if (rebateType == Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY) {
            totalRebate.setUnAccountEntryTotalNum(totalRebate.getUnAccountEntryTotalNum() + rebateNum);
        } else {
            totalRebate.setAccountEntryTotalNum(totalRebate.getAccountEntryTotalNum() + rebateNum);
        }
        totalRebateApiRepository.saveAndFlush(totalRebate);

    }
}
