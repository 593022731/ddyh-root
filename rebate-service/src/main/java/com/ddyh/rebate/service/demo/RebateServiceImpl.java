package com.ddyh.rebate.service.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sonnhe.mall_extra_rebate.entity.constant.CharacterType;
import com.sonnhe.mall_extra_rebate.entity.constant.Constant;
import com.sonnhe.mall_extra_rebate.entity.constant.URLConstant;
import com.sonnhe.mall_extra_rebate.entity.dto.exchange.ExchangeTimesAndQuotaDto;
import com.sonnhe.mall_extra_rebate.entity.dto.exchange.ExchangeTotalDto;
import com.sonnhe.mall_extra_rebate.entity.dto.exchange.ServerAndExchangeInfoDto;
import com.sonnhe.mall_extra_rebate.entity.po.*;
import com.sonnhe.mall_extra_rebate.entity.result.JsonResult;
import com.sonnhe.mall_extra_rebate.entity.result.PageResult;
import com.sonnhe.mall_extra_rebate.entity.result.ResultCode;
import com.sonnhe.mall_extra_rebate.entity.vo.ApplyExchangeVo;
import com.sonnhe.mall_extra_rebate.entity.vo.PageVo;
import com.sonnhe.mall_extra_rebate.exception.business.BusinessException;
import com.sonnhe.mall_extra_rebate.exception.business.ParameterInvalidException;
import com.sonnhe.mall_extra_rebate.exception.business.RemoteAccessException;
import com.sonnhe.mall_extra_rebate.repository.*;
import com.sonnhe.mall_extra_rebate.server.IdentityServer;
import com.sonnhe.mall_extra_rebate.service.RebateService;
import com.sonnhe.mall_extra_rebate.utils.DateUtil;
import com.sonnhe.mall_extra_rebate.utils.RedisConstant;
import com.sonnhe.mall_extra_rebate.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 额外返利兑换，查询等相关逻辑
 */
@Service
public class RebateServiceImpl implements RebateService {

    @Autowired
    private IdentityServer identityServer;

    @Autowired
    private RebateRepository rebateRepository;

    @Autowired
    private RebateAlreadyRepository rebateAlreadyRepository;

    @Autowired
    private TotalRebateRepository totalRebateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Resource
    private DictionaryRepository dictionaryRepository;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private DistributorRepository distributorRepository;

    @Value("${server.A.domain}")
    private String aSystemDomain;

    @Value("${server.A.prefix}")
    private String aSystemPrefix;

    @Value("${break.extra.rebate.start.time}")
    private String extraRebateStartTime;

    @Override
    public ExchangeTotalDto getExchangeTotalNum() {
        BigDecimal res = new BigDecimal(getSurplusNum(false)).movePointLeft(2);
        return new ExchangeTotalDto(res);
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public void applyExchange(ApplyExchangeVo applyRebateVo) {
        int applyCount = applyRebateVo.getCount();
        // 兑换额度限定，1000以上，并以1000为增长
        if (applyCount < 1000 || applyCount % 1000 > 0) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }

        User currentUser = identityServer.getCurrentUser();
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
        isOutNumberLimitAmount(currentUser.getUid(), dataList.get(0), applyRebateVo.getCount());

        Long uid = currentUser.getUid();
        // 1. 查询未复核数
        TotalRebate userTotalRebate = totalRebateRepository.findTotalRebateByRebateUserIdAndRebateUserType(uid,
                Constant.USER_REBATE_TYPE_MEMBER);
        if (userTotalRebate == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        int totalAbleRebateNum;     // 总可兑换数
        int totalUnCheck;           // 总未复核数
        int userAbleRebateNum = userTotalRebate.getAccountEntryTotalNum() - userTotalRebate.getRebatedNum();
        int userUnCheck = userTotalRebate.getAccountEntryTotalNum() - userTotalRebate.getCheckNum();
        if (userAbleRebateNum < 0) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
        TotalRebate channelTotalRebate = null;
        // 若用户为顶级用户，则将渠道返利纳入计算
        if (currentUser.getCharacterType() == CharacterType.CHANNEL_TYPE.getType()) {
            channelTotalRebate = totalRebateRepository.findTotalRebateByRebateUserIdAndRebateUserType(currentUser.getChannelId(),
                    Constant.USER_REBATE_TYPE_CHANNEL);
            if (channelTotalRebate == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND);
            }
            int channelAbleNum = channelTotalRebate.getAccountEntryTotalNum() - channelTotalRebate.getRebatedNum();
            if (channelAbleNum < 0) {
                throw new BusinessException(ResultCode.SERVER_ERROR);
            }
            totalAbleRebateNum = userAbleRebateNum + channelAbleNum;
            totalUnCheck = userUnCheck + channelTotalRebate.getAccountEntryTotalNum() - channelTotalRebate.getCheckNum();
        } else {
            totalAbleRebateNum = userAbleRebateNum;
            totalUnCheck = userUnCheck;
        }
        if (applyCount > totalAbleRebateNum) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        // 2. 查询所有未复核单
        List<Rebate> list = rebateRepository.findByRewardUserIdAndStateAndRebateUserType(uid,
                Constant.REBATE_STATE_UNCHECKED, Constant.USER_REBATE_TYPE_MEMBER);
        if (currentUser.getCharacterType() == CharacterType.CHANNEL_TYPE.getType()) {
            List<Rebate> channelRebateList = rebateRepository.findByRewardUserIdAndStateAndRebateUserType(currentUser.getChannelId(),
                    Constant.REBATE_STATE_UNCHECKED, Constant.USER_REBATE_TYPE_CHANNEL);
            list.addAll(channelRebateList);
        }
        int unCheckNum = 0;
        if (list != null && list.size() > 0) {
            for (Rebate rebate : list) {
                unCheckNum += rebate.getRebateNum();
                rebate.setState(Constant.REBATE_STATE_CHECKED);
                rebateRepository.save(rebate);
            }
        }
        // 3. 比较未复核数与所有未复核单总数
        if (totalUnCheck != unCheckNum) {
            throw new BusinessException(ResultCode.REPEAT_CHECK_ERROR);
        }
        // 4. 计算额外返利余额
        int surplusNum = getSurplusNum(false);
        // 5. 查看余额是否大于/等于本次兑换数
        if (surplusNum < applyCount) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        // 6. 更新复核数
        Date now = new Date();
        userTotalRebate.setCheckNum(userTotalRebate.getAccountEntryTotalNum());
        userTotalRebate.setLastRebateTime(now);
        // 这里需要进行拆分计算（优先扣除顶级用户的返利数，不够再扣除渠道的返利数）
        if (currentUser.getCharacterType() == CharacterType.CHANNEL_TYPE.getType()) {
            if (userAbleRebateNum >= applyCount) {
                userTotalRebate.setRebatedNum(userTotalRebate.getRebatedNum() + applyCount);
            } else {
                userTotalRebate.setRebatedNum(userTotalRebate.getAccountEntryTotalNum());
                assert channelTotalRebate != null;
                channelTotalRebate.setCheckNum(channelTotalRebate.getAccountEntryTotalNum());
                channelTotalRebate.setRebatedNum(channelTotalRebate.getRebatedNum() + applyCount - userAbleRebateNum);
                totalRebateRepository.save(channelTotalRebate);
            }
        } else {
            userTotalRebate.setRebatedNum(userTotalRebate.getRebatedNum() + applyCount);
        }
        totalRebateRepository.save(userTotalRebate);
        // 7. 新建返利数
        RebateAlready rebateAlready = new RebateAlready(currentUser.getUid(), applyCount, now,
                Constant.APPLY_REBATE_STATE_COMMIT, Constant.USER_REBATE_TYPE_MEMBER, Constant.REBATE_ALREADY_FROM_B, currentUser.getPhone());
        rebateAlreadyRepository.save(rebateAlready);
    }

    private void isOutNumberLimitAmount(long uid, Dictionary amountLimit, int applyNum) {
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
        int rebateAlreadyTotalNum = rebateAlreadyRepository.getRebateAlreadyTotalNumByRebateTime(destUserPhone, rebateAlreadyState,
                thisMonthStartTime, thisMonthEndTime);

        if (rebateAlreadyTotalNum + applyNum >= Integer.valueOf(amountLimit.getDataValue())) {
            throw new BusinessException(ResultCode.REBATE_APPLY_OUT_NUMBER);
        }
    }

    @Override
    public ExchangeTimesAndQuotaDto getExchangeTimesAndQuota() {
        User currentUser = identityServer.getCurrentUser();
        return rebateAlreadyRepository.getExchangeTimesAndQuota(currentUser.getUid(), Constant.APPLY_REBATE_STATE_EXCHANGE,
                Constant.REBATE_ALREADY_FROM_B);
    }

    @Override
    public PageResult<RebateAlready> getRebateAlreadyList(PageVo pageVo) {
        User currentUser = identityServer.getCurrentUser();
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(pageVo.getCurrentPage() - 1, pageVo.getPageSize(), sort);
        Page<RebateAlready> page = rebateAlreadyRepository.findAllByRebateUserIdAndRebateFromAndRebateUserType(currentUser.getUid(), Constant.REBATE_ALREADY_FROM_B, Constant.USER_REBATE_TYPE_MEMBER, pageable);
        List<RebateAlready> content = page.getContent();
        long total = page.getTotalElements();
        return new PageResult<>(total, content);
    }

    @Override
    public ServerAndExchangeInfoDto getServerAndExchangeInfo() {
        // 添加token
        User currentUser = identityServer.getCurrentUser();
        String openId = currentUser.getOpenId();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", openId);
        map.add("appType", "4");
        String url = aSystemDomain + aSystemPrefix + URLConstant.SERVER_EXCHANGING_INFO_URL;
        RequestEntity requestEntity = new RequestEntity(map, HttpMethod.GET, URI.create(url));
        ResponseEntity<Map> entity = restTemplate.exchange(requestEntity, Map.class);
        int statusCodeValue = entity.getStatusCodeValue();
        if (statusCodeValue != 200) {
            throw new RemoteAccessException(ResultCode.REQUEST_ERROR);
        }
        JsonResult jsonResult = JSONObject.parseObject(JSON.toJSONString(entity.getBody()), JsonResult.class);
        Integer code = jsonResult.getCode();
        if (code != 1) {
            throw new RemoteAccessException(ResultCode.REQUEST_DEAL_ERROR);
        }
        Object res = jsonResult.getData();
//        parseOrderSystemEntity(entity);
//        JsonResult body = entity.getBody();
//        Object res = body.getData();
        if (res == null) {
            return null;
        }
        System.out.println("res = " + res.toString());
        return JSONObject.parseObject(res.toString(), ServerAndExchangeInfoDto.class);
//        return body.getData() == null ? null : (ServerAndExchangeInfoDto)body.getData();
    }

    /**
     * 计算额外返利余额
     * @param all 是否计算未入账金额
     */
    @Override
    public int getSurplusNum(boolean all) {
        // 1. 计算返利拆分日期之后所有的额外返利单子数
        User currentUser = identityServer.getCurrentUser();
        Long uid = currentUser.getUid();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime;
        try {
            startTime = sdf.parse(extraRebateStartTime);
        } catch (ParseException e) {
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
        Set<Short> rebateState = new HashSet<>();
        if (all) {
            rebateState.add(Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY);
        }
        rebateState.add(Constant.REBATE_STATE_UNCHECKED);
        rebateState.add(Constant.REBATE_STATE_CHECKED);

        //先计算分销商
        //分销商返利类型 3和7
        Set<Short> rebateType = new HashSet<>();
        rebateType.add(Constant.REBATE_TYPE_GIFT_EXTRA);
        rebateType.add(Constant.REBATE_TYPE_JD_EXTRA);
        int extraRebateTotalNum = rebateRepository.getExtraRebateTotalNum(uid, rebateState,
                Constant.USER_REBATE_TYPE_MEMBER, rebateType, startTime);

        //如果是联合创始人，需要加上渠道商的收益
        if (currentUser.getChannelId() != null && currentUser.getCharacterType() == Constant.USER_CHANNEL_PROVIDER_TYPE) {
            //渠道商返利类型  4和8
            rebateType = new HashSet<>();
            rebateType.add(Constant.REBATE_TYPE_GIFT_CHANNEL);
            rebateType.add(Constant.REBATE_TYPE_JD_CHANNEL);
            extraRebateTotalNum += rebateRepository.getChannelRebateTotalNum(currentUser.getChannelId(), rebateState,
                    Constant.USER_REBATE_TYPE_CHANNEL, rebateType);
        }

        // 2. 计算所有已返利/已申请额度
        Set<Short> rebateAlreadyState = new HashSet<>();
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_COMMIT);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_CHECKED);
        rebateAlreadyState.add(Constant.APPLY_REBATE_STATE_EXCHANGE);
        int extraRebateAlreadyNum = rebateAlreadyRepository.getExtraRebateAlreadyNum(uid, rebateAlreadyState, Constant.REBATE_ALREADY_FROM_B);
        return extraRebateTotalNum - extraRebateAlreadyNum;
    }
}
