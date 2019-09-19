package com.ddyh.rebate.service.demo;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 额外返利统计查询
 */
@Slf4j
@Service
public class InfoStatisticsServiceImpl implements InfoStatisticsService {

    private final IdentityServer identityServer;

    private final RebateRepository rebateRepository;

    private final OrderRepository orderRepository;

    private final MemberTeamRepository memberTeamRepository;

    private final RebateService rebateService;

    private final UserRepository userRepository;

    private final RebateRepositoryImpl rebateRepositoryImpl;


    @Value("${break.extra.rebate.start.time}")
    private String extraRebateStartTime;

    @Value("${profit.start.time}")
    private String profitStartTime;

    private static Set<Short> userExtraRebateType = new HashSet<>();

    private static Set<Short> channelRebateType = new HashSet<>();

    private static Set<Short> ableRebateState = new HashSet<>();

    private static Set<Short> unAccountRebateState = new HashSet<>();

    static {
        // 初始化用户额外返利类型，3 和 7
        userExtraRebateType.add(Constant.REBATE_TYPE_GIFT_EXTRA);
        userExtraRebateType.add(Constant.REBATE_TYPE_JD_EXTRA);
        // 初始化渠道返利类型，4 和 8
        channelRebateType.add(Constant.REBATE_TYPE_GIFT_CHANNEL);
        channelRebateType.add(Constant.REBATE_TYPE_JD_CHANNEL);
        // 初始化可用返利状态，0，1，2
        ableRebateState.add(Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY);
        ableRebateState.add(Constant.REBATE_STATE_UNCHECKED);
        ableRebateState.add(Constant.REBATE_STATE_CHECKED);
        // 初始化未入账返利状态，0
        unAccountRebateState.add(Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY);
    }

    @Autowired
    public InfoStatisticsServiceImpl(IdentityServer identityServer, RebateRepository rebateRepository, OrderRepository orderRepository, MemberTeamRepository memberTeamRepository, RebateService rebateService, UserRepository userRepository, RebateRepositoryImpl rebateRepositoryImpl) {
        this.identityServer = identityServer;
        this.rebateRepository = rebateRepository;
        this.orderRepository = orderRepository;
        this.memberTeamRepository = memberTeamRepository;
        this.rebateService = rebateService;
        this.userRepository = userRepository;
        this.rebateRepositoryImpl = rebateRepositoryImpl;
    }

    @Override
    public MyProfitDto getMyProfit() {
        User currentUser = identityServer.getCurrentUser();

        //先计算分销商的收益
        //分销商返利类型 ,3和7
        MyProfitDto myProfitDto = getMyProfit(currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER, userExtraRebateType);

        //如果是联合创始人，需要加上渠道商的收益
        if (currentUser.getChannelId() != null && currentUser.getCharacterType() == Constant.USER_CHANNEL_PROVIDER_TYPE) {
            //渠道商返利类型  4和8
            MyProfitDto channelDto = getMyProfit(currentUser.getChannelId(), Constant.USER_REBATE_TYPE_CHANNEL, channelRebateType);
            BigDecimal todayProfit = myProfitDto.getTodayProfit().add(channelDto.getTodayProfit());
            myProfitDto.setTodayProfit(todayProfit);

            BigDecimal mouthProfit = myProfitDto.getMouthProfit().add(channelDto.getMouthProfit());
            myProfitDto.setMouthProfit(mouthProfit);

            BigDecimal allProfit = myProfitDto.getAllProfit().add(channelDto.getAllProfit());
            myProfitDto.setAllProfit(allProfit);

            BigDecimal unAccountEntryProfit = myProfitDto.getUnAccountEntryProfit().add(channelDto.getUnAccountEntryProfit());
            myProfitDto.setUnAccountEntryProfit(unAccountEntryProfit);
        }

        // 账户余额
        myProfitDto.setTotalProfit(new BigDecimal(rebateService.getSurplusNum(false)).movePointLeft(2));
        return myProfitDto;
    }

    /**
     * 计算 今日收益、本月收益、 累计收益、待入账收益
     *
     * @param uid
     * @param rebateUserType
     * @return
     */
    private MyProfitDto getMyProfit(Long uid, short rebateUserType, Set<Short> rebateType) {
        BigDecimal zero = new BigDecimal(0);
        MyProfitDto myProfitDto = new MyProfitDto();
        Date now = new Date();

        //返利状态,今日收益、本月收益、累计收益，都是不包含已取消状态的
        // 今日收益
        BigDecimal todayProfit = rebateRepository.findProfitByDateAndState(uid, rebateUserType, rebateType,
                TimeUtils.getDateStartTime(now), now, ableRebateState);
        myProfitDto.setTodayProfit(todayProfit == null ? zero : todayProfit);

        // 本月收益
        BigDecimal mouthProfit = rebateRepository.findProfitByDateAndState(uid, rebateUserType, rebateType,
                TimeUtils.getDateMonthStartTime(now), now, ableRebateState);
        myProfitDto.setMouthProfit(mouthProfit == null ? zero : mouthProfit);


        // 分销商需加上时间限制 联合创始人不需要


        // 返利状态
        BigDecimal unAccountEntryProfit = null;

        // 分销商加6月1号限制
        if (Constant.USER_REBATE_TYPE_MEMBER == rebateUserType) {
            Date extraStartTime = TimeUtils.formatDateStr(extraRebateStartTime, TimeUtils.YYYMM_DD_HHMMSS);

            // 累计收益
            BigDecimal allProfit = rebateRepository.findProfitByDateAndState(uid, rebateUserType, rebateType, extraStartTime, now, ableRebateState);
            myProfitDto.setAllProfit(allProfit == null ? zero : allProfit);

            // 待入账
            int extraRebateTotalNum = rebateRepository.getExtraRebateTotalNum(uid, unAccountRebateState,
                    Constant.USER_REBATE_TYPE_MEMBER, rebateType, extraStartTime);
            unAccountEntryProfit = new BigDecimal(extraRebateTotalNum).movePointLeft(2);

//            unAccountEntryProfit = rebateRepository.findProfitByDateAndState(uid, rebateUserType, jdRebateType,
//                    TimeUtils.getDateStartTime(TimeUtils.formatDateStr(extraRebateStartTime, TimeUtils.YYYMM_DD_HHMMSS)), now, unAccountRebateState);
        } else {
            // 累计收益
            BigDecimal allProfit = rebateRepository.findAllProfitByState(uid, rebateUserType, rebateType, ableRebateState);
            myProfitDto.setAllProfit(allProfit == null ? zero : allProfit);

            // 待入账
            unAccountEntryProfit = rebateRepository.findAllProfitByState(uid, rebateUserType, rebateType, unAccountRebateState);
        }
        myProfitDto.setUnAccountEntryProfit(unAccountEntryProfit == null ? zero : unAccountEntryProfit);
        return myProfitDto;
    }

    @Override
    public TeamSaleDto teamSale() {
        User currentUser = identityServer.getCurrentUser();
        String path = currentUser.getPath();
        if (Strings.isNullOrEmpty(path)) {
            throw new BusinessException("不存在下级");
        }
        BigDecimal zero = new BigDecimal(0);
        TeamSaleDto teamSaleDto = new TeamSaleDto(zero, zero, 0, zero);
        Date now = new Date();
        // 今日销售额
        BigDecimal todaySaleQuota = orderRepository.countLowLevelOrderSaleByDate(path, TimeUtils.getDateStartTime(now), now);
        teamSaleDto.setTodaySaleQuota(todaySaleQuota == null ? zero : todaySaleQuota);
        // 本月销售额
        BigDecimal mouthSaleQuota = orderRepository.countLowLevelOrderSaleByDate(path, TimeUtils.getDateMonthStartTime(now), now);
        teamSaleDto.setMouthSaleQuota(mouthSaleQuota == null ? zero : mouthSaleQuota);

        // 当月购买用户数
        TeamSaleBo teamSaleBo = orderRepository.countBuyOrderUserNumByDate(path, TimeUtils.getDateMonthStartTime(now), now);

        // 分销商当月预估奖励(没有分类类型条件，相当于1~8都查询，A+B系统的所有返利)
        BigDecimal extraRebateTotalNum = rebateRepository.getExtraexpectReward(currentUser.getUid(), ableRebateState,
                Constant.USER_REBATE_TYPE_MEMBER, TimeUtils.getDateMonthStartTime(now), now);

        //如果是联合创始人，需要加上渠道商的收益
        if (currentUser.getChannelId() != null && currentUser.getCharacterType() == Constant.USER_CHANNEL_PROVIDER_TYPE) {
            BigDecimal channelNum = rebateRepository.getExtraexpectReward(currentUser.getChannelId(), ableRebateState,
                    Constant.USER_REBATE_TYPE_CHANNEL, channelRebateType, TimeUtils.getDateMonthStartTime(now), now);
            extraRebateTotalNum = extraRebateTotalNum.add(channelNum);
        }

        log.info("old=" + teamSaleBo.getSaleQuota() + ",new=" + extraRebateTotalNum);

        teamSaleDto.setExpectReward(extraRebateTotalNum);
        teamSaleDto.setBuyUserNum(teamSaleBo.getUserCount());
        return teamSaleDto;
    }

    @Override
    public TeamInfoDto teamInfo() {
        TeamInfoDto teamInfoDto = new TeamInfoDto(0, 0, 0, 0);
        // 总人数
        User currentUser = identityServer.getCurrentUser();
        MemberTeam memberTeam = memberTeamRepository.findMemberTeamByUserId(currentUser.getUid());
        if (memberTeam == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_FOUND);
        }
        teamInfoDto.setTotalNum(memberTeam.getLowLevelMemberNum() + 1);//团队成员包含自己，所以要加1，产品定义自己为团队长，所以也算在团队里面
        // 已开单
        String path = currentUser.getPath();
        if (Strings.isNullOrEmpty(path)) {
            throw new BusinessException("不存在下级");
        }
        teamInfoDto.setBuyNum(orderRepository.countBookOrderUser(path));
        // 今日新增
        Date now = new Date();
        teamInfoDto.setTodayIncr(userRepository.getRegisterUserNumByDate(path, TimeUtils.getDateStartTime(now), now));
        // 本月新增
        teamInfoDto.setMouthIncr(userRepository.getRegisterUserNumByDate(path, TimeUtils.getDateMonthStartTime(now), now));
        return teamInfoDto;
    }

    @Override
    public IdentityCountDto getTeamIdentityCount() {
        User currentUser = identityServer.getCurrentUser();
        MemberTeam memberTeam = memberTeamRepository.findMemberTeamByUserId(currentUser.getUid());
        if (memberTeam == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_FOUND);
        }
        return memberTeamRepository.getTeamIdentityCount(currentUser.getPath());
    }

    @Override
    public PageResult<TeamUserDto> getTeamUserList(PageVo pageVo, String nickName) {
        List<TeamUserDto> res = new ArrayList<>();
        User currentUser = identityServer.getCurrentUser();
        String userPath = currentUser.getPath();
        // 获取用户下的用户列表
        List<Long> uidList = userRepository.getLowLevelUserList(userPath, pageVo, nickName);
        if (uidList.size() < 1) {
            return new PageResult<>(0L, res);
        }

        Date now = new Date();
        Date weekTime = TimeUtils.getDateWeekStartTime(now);
        Date mouthTime = TimeUtils.getDateMonthStartTime(now);

        BigDecimal zero = new BigDecimal(0);
        uidList.forEach(uid -> {
            // 查询用户基本信息
            TeamUserDto teamUserDto = CommonUtils.dataHandler(userRepository.getTeamUserInfoByUid(uid), new TeamUserDto());
            // 处理手机号
            teamUserDto.setPhone(CommonUtils.hidePhoneNum(teamUserDto.getPhone()));
            // 获取团队人数
            MemberTeam memberTeam = memberTeamRepository.findMemberTeamByUserId(uid);
            if (memberTeam == null) {
                teamUserDto.setTeamNum(0);
            } else {
                teamUserDto.setTeamNum(memberTeam.getLowLevelMemberNum());
            }
            // 获取7天收益
            //分销商
            BigDecimal weekProfit = rebateRepository.findProfitByDateAndState(uid, Constant.USER_REBATE_TYPE_MEMBER, userExtraRebateType,
                    weekTime, now, ableRebateState);
            weekProfit = weekProfit == null ? zero : weekProfit;

            //要加上渠道商(联创)
            if (memberTeam.getIdentity() == IdentityConstant.CO_FOUNDER.getIdentity() && memberTeam.getChannelId() != null) {
                BigDecimal weekProfitCh = rebateRepository.findProfitByDateAndState(memberTeam.getChannelId(), Constant.USER_REBATE_TYPE_CHANNEL, channelRebateType,
                        weekTime, now, ableRebateState);
                weekProfitCh = weekProfitCh == null ? zero : weekProfitCh;
                weekProfit = weekProfit.add(weekProfitCh);
            }

            // 获取月收益
            BigDecimal mouthProfit = rebateRepository.findProfitByDateAndState(uid, Constant.USER_REBATE_TYPE_MEMBER, userExtraRebateType,
                    mouthTime, now, ableRebateState);
            mouthProfit = mouthProfit == null ? zero : mouthProfit;
            //要加上渠道商(联创)
            if (memberTeam.getIdentity() == IdentityConstant.CO_FOUNDER.getIdentity() && memberTeam.getChannelId() != null) {
                BigDecimal mouthProfitCh = rebateRepository.findProfitByDateAndState(memberTeam.getChannelId(), Constant.USER_REBATE_TYPE_CHANNEL, channelRebateType,
                        mouthTime, now, ableRebateState);
                mouthProfitCh = mouthProfitCh == null ? zero : mouthProfitCh;
                mouthProfit = mouthProfit.add(mouthProfitCh);
            }

            teamUserDto.setWeekProfit(weekProfit);
            teamUserDto.setMouthProfit(mouthProfit);
            User user = userRepository.findOne(uid);
            //获取月销售额
            BigDecimal mouthSaleQuota = orderRepository.countLowLevelOrderSaleByDate(user.getPath(), mouthTime, now);
            teamUserDto.setMouthSaleQuota(mouthSaleQuota == null ? zero : mouthSaleQuota);
            res.add(teamUserDto);
        });
        long total = userRepository.countLowLevelNum(userPath, nickName);
        return new PageResult<>(total, res);
    }

    @Override
    public PageResult<AchievementDto> statisticsPastAchievement(PageVo pageVo) {
        Integer pageSize = pageVo.getPageSize();
        Integer currentPage = pageVo.getCurrentPage();
        Date now = new Date();
        SimpleDateFormat counterFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat resFormat = new SimpleDateFormat("yy-MM");
        // 获取项目交易开始时间
        Date startTime;
        try {
            startTime = counterFormat.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
        User currentUser = identityServer.getCurrentUser();
        String path = currentUser.getPath();
        // 获取当前时间与开始时间的月份差，等到总条数
        int total = TimeUtils.getTwoDateMonthInterval(startTime, now) + 1;
        PageResult<AchievementDto> pageResult = new PageResult<>();
        pageResult.setTotalCount((long) total);
        List<AchievementDto> res = new ArrayList<>();
        // 获取统计的日期时间
        Date counterTime = TimeUtils.getSeveralMonthBeforeOrAfter(now, -(currentPage - 1) * pageSize);
        for (int i = 0; i < pageSize; i++) {
            // 获取统计时间当前月的起始时间
            Date dateMonthStartTime = TimeUtils.getDateMonthStartTime(counterTime);
            // 若超过交易开始时间，结束统计
            if (startTime.compareTo(dateMonthStartTime) > 0) {
                break;
            }
            // 获取统计时间当前月的截止时间
            Date dateMonthEndTime = TimeUtils.getDateMonthEndTime(counterTime);
            // 计算数量
            BigDecimal quota = orderRepository.countLowLevelOrderSaleByDate(path, dateMonthStartTime, dateMonthEndTime);
            res.add(new AchievementDto(resFormat.format(dateMonthStartTime), quota));
            counterTime = TimeUtils.getSeveralMonthBeforeOrAfter(counterTime, -1);
        }
        pageResult.setDataList(res);
        return pageResult;
    }

    @Override
    public PageResult<ProfitDto> statisticsPastProfit(PageVo pageVo) {
        Integer pageSize = pageVo.getPageSize();
        Integer currentPage = pageVo.getCurrentPage();
        Date now = new Date();
        SimpleDateFormat counterFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat resFormat = new SimpleDateFormat("yy-MM");
        // 获取项目交易开始时间
        Date startTime;
        Date extraStartTime;
        try {
            startTime = counterFormat.parse(profitStartTime);
            extraStartTime = counterFormat.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
        User currentUser = identityServer.getCurrentUser();
        // 获取当前时间与开始时间的月份差，等到总条数
        int total = TimeUtils.getTwoDateMonthInterval(startTime, now) + 1;
        PageResult<ProfitDto> pageResult = new PageResult<>();
        pageResult.setTotalCount((long) total);
        List<ProfitDto> res = new ArrayList<>();
        // 获取统计的日期时间
        Date counterTime = TimeUtils.getSeveralMonthBeforeOrAfter(now, -(currentPage - 1) * pageSize);
        BigDecimal zero = BigDecimal.ZERO;
        for (int i = 0; i < pageSize; i++) {
            // 获取统计时间当前月的起始时间
            Date dateMonthStartTime = TimeUtils.getDateMonthStartTime(counterTime);
            // 获取统计时间当前月的截止时间
            Date dateMonthEndTime = TimeUtils.getDateMonthEndTime(counterTime);
            // 若超过交易开始时间，结束统计
            if (startTime.compareTo(dateMonthStartTime) > 0) {
                break;
            }
            // 返利类型
            BigDecimal profit = zero;
            //如果是联合创始人，需要加上渠道商的收益
            if (currentUser.getChannelId() != null && currentUser.getCharacterType() == Constant.USER_CHANNEL_PROVIDER_TYPE) {
                //渠道商返利类型  4和8
                BigDecimal channelProfit = rebateRepository.findProfitByDateAndState(currentUser.getChannelId(), Constant.USER_REBATE_TYPE_CHANNEL,
                        channelRebateType, dateMonthStartTime, dateMonthEndTime, ableRebateState);
                channelProfit = channelProfit == null ? zero : channelProfit;
                profit = profit.add(channelProfit);
            }
            // 计算额外返利开始时间
            if (dateMonthStartTime.compareTo(extraStartTime) >= 0) {
                BigDecimal extraProfit = rebateRepository.findProfitByDateAndState(currentUser.getUid(), Constant.USER_REBATE_TYPE_MEMBER,
                        userExtraRebateType, dateMonthStartTime, dateMonthEndTime, ableRebateState);
                profit = profit.add(extraProfit == null ? zero : extraProfit);
            }
            res.add(new ProfitDto(resFormat.format(dateMonthStartTime), profit));
            counterTime = TimeUtils.getSeveralMonthBeforeOrAfter(counterTime, -1);
        }
        pageResult.setDataList(res);
        return pageResult;
    }


    @Override
    public PageResult<ProfitMonthDetailDto> statisticsPastProfitMonthDetail(PageVo pageVo, String destMonth) {
        User currentUser = identityServer.getCurrentUser();
        Date dateMonthStartTime = TimeUtils.getDateFormatYYMM(destMonth);
        Date dateMonthEndTime = TimeUtils.getDateMonthEndTime(dateMonthStartTime);

        SimpleDateFormat counterFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取项目交易开始时间
        Date startTime;
        try {
            startTime = counterFormat.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }

        List<ProfitMonthDetailDto> list = rebateRepositoryImpl.getRebateMonthDetail(currentUser, startTime,
                dateMonthStartTime, dateMonthEndTime, pageVo);
        Long totalNum = rebateRepositoryImpl.countRebateMonthDetail(currentUser, startTime,
                dateMonthStartTime, dateMonthEndTime);

        return new PageResult<>(totalNum, list);
    }

    @Override
    public PageResult<SalesMonthDetailDto> statisticsPastSalesMonthDetail(PageVo pageVo, String destMonth) {
        User currentUser = identityServer.getCurrentUser();
        String path = currentUser.getPath();
        Set<Short> rebateType = new HashSet<>(userExtraRebateType);
        //如果是联合创始人，需要加上渠道商的收益
        if (currentUser.getChannelId() != null && currentUser.getCharacterType() == Constant.USER_CHANNEL_PROVIDER_TYPE) {
            //渠道商返利类型  4和8
            rebateType.addAll(channelRebateType);
        }
        Date dateMonthStartTime = TimeUtils.getDateFormatYYMM(destMonth);
        Date dateMonthEndTime = TimeUtils.getDateMonthEndTime(dateMonthStartTime);

        SimpleDateFormat counterFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime;
        try {
            startTime = counterFormat.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }

        //销售详情的收益，只显示对于这个订单返利给自己的收益
        List<SalesMonthDetailDto> list = rebateRepositoryImpl.getSalesMonthDetail(currentUser, rebateType, dateMonthStartTime, dateMonthEndTime, startTime, pageVo);
        Long totalNum = rebateRepositoryImpl.countSalesMonthDetail(path, dateMonthStartTime, dateMonthEndTime);

        return new PageResult<>(totalNum, list);
    }

    @Override
    public Short getIdentity() {
        User currentUser = identityServer.getCurrentUser();
        MemberTeam memberTeam = memberTeamRepository.findMemberTeamByUserId(currentUser.getUid());
        return memberTeam.getIdentity();
    }
}
