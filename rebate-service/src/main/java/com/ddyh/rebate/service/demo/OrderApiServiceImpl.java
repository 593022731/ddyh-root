package com.ddyh.rebate.service.demo;


import jdk.nashorn.internal.ir.annotations.Reference;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单部分逻辑与A系统返利计算
 */
@Service
@Slf4j
public class OrderApiServiceImpl implements OrderApiService {

    @Value("${constant.test.data}")
    private boolean IS_TEST;

    @Value("${constant.extra.rebate.url}")
    private String extraRebateUrl;

    @Value("${constant.product.prefix.url}")
    private String productUrlPrefix;

    @Value("${product.system.url}")
    private String productSystemUrl;

    @Value("${constant.message.on}")
    private boolean isSendMsg;

    @Resource
    private OrderApiRepository orderApiRepository;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private OrderTimerService orderTimerService;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private IdentityServer identityServer;
    @Resource
    private UserApiRepository userApiRepository;
    @Resource
    private MemberRepository memberRepository;
    @Resource
    private RateApiRepository rateApiRepository;
    @Resource
    private RebateApiRepository rebateApiRepository;
    @Resource
    private ChannelProviderRepository channelProviderRepository;
    @Resource
    private TaxationApiRepository taxationApiRepository;
    @Resource
    private TotalRebateApiRepository totalRebateApiRepository;

    @Resource
    private MemberTeamService memberTeamService;
    @Resource
    private PreBuyRepository preBuyRepository;
    @Resource
    private PreBuyItemRepository preBuyItemRepository;
    @Resource
    private ShoppingCartRepository shoppingCartRepository;
    @Resource
    private ShoppingCartItemRepository shoppingCartItemRepository;
    @Resource
    private AfterSaleRepository afterSaleRepository;
    @Resource
    private OrderItemRepository orderItemRepository;
    @Resource
    private AfterSaleRefundRepository afterSaleRefundRepository;
    @Resource
    private ShoppingCartService shoppingCartService;
    @Resource
    private MemberTeamRepository memberTeamRepository;
    @Resource
    private DictionaryRepository dictionaryRepository;
    @Resource
    private PreBuyService preBuyService;
    @Resource
    private UserAddressApiService addressApiService;
    @Resource
    private OperaLogRepository operaLogRepository;
    @Resource
    private RebateExpRepository rebateExpRepository;
    @Resource
    private PreBuyOrderRepository preBuyOrderRepository;
    @Resource
    private ExpCardOrderRepository expCardOrderRepository;
    @Reference
    private ProductFacade productFacade;
    @Reference
    private JDStockFacade jdStockFacade;
    @Resource
    private RetryBuilder retryBuilder;
    @Resource
    private AsyncEventBus eventBus;
    @Reference
    private PayFacade payFacade;
    @Resource
    private RebateApiService rebateApiService;

    private static final List<ProductAfterSaleTypeTo> SALE_TYPES = new ArrayList<>(6);
    private static final Map<String, String> RETURN_TYPES = new HashMap<>(4);

    static {
        SALE_TYPES.add(new ProductAfterSaleTypeTo("10", "退货"));
        SALE_TYPES.add(new ProductAfterSaleTypeTo("20", "换货"));
        SALE_TYPES.add(new ProductAfterSaleTypeTo("30", "维修"));

        /**
         * 取件方式, 4-上门取件, 7-客户送货, 40-客户发货
         */
        RETURN_TYPES.put("4", "上门取件");
        RETURN_TYPES.put("7", "送货至自提点");
        RETURN_TYPES.put("40", "快递至京东");
    }

    /**
     * 根据订单状态查询订单列表
     *
     * @param status      订单状态，-1 为所有订单
     * @param orderPageVo 分页vo
     * @return PageResult
     */
    @Override
    public PageResult<Order> findOrdersByStatus(Short status, OrderPageVo orderPageVo) {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(orderPageVo.getCurrentPage() - 1, orderPageVo.getPageSize(), sort);
        User currentUser = identityServer.getCurrentUser();

        // 查询该用户是否存在未完成订单
        monitorAndUpdateOrderState(currentUser.getUid().toString());

        Page<Order> page;
        if (status == Constant.ORDER_STATUS_ALL) {
            page = orderApiRepository.findAllByUserAndState(currentUser, (short) 0, pageable);
        } else {
            page = orderApiRepository.findAllByUserAndOrderStatusAndState(currentUser, status, (short) 0, pageable);
        }

        List<Order> content = page.getContent();
        // 检测订单是否过期，查询所有订单或未支付订单列表时（服务器重启后部分订单未能更改状态的最后保证）
        if (status == Constant.ORDER_STATUS_ALL || status == Constant.ORDER_STATUS_NOT_PAY) {
            if (content != null && content.size() > 0) {
                Date now = new Date();
                for (Order order : content) {
                    if (order.getOrderStatus() != Constant.ORDER_STATUS_NOT_PAY) {
                        continue;
                    }
                    Date orderTime = order.getOrderTime();
                    // 判断未支付订单是否超时
                    boolean res = CommonUtil.timeLaterCompareIsExpire(orderTime, Constant.ORDER_TIMER_EFFECTIVE, now);
                    if (res) {
                        // 将订单状态修改为取消
                        order.setOrderStatus(Constant.ORDER_STATUS_CANCELED);
                        orderApiRepository.save(order);
                    }
                }
            }
        }
        long totalCount = page.getTotalElements();
        return new PageResult<>(totalCount, content);
    }

    /**
     * 订单详情查询
     *
     * @param id 订单id
     * @return JsonResult, data为 OrderDto
     */
    @Override
    public Order findOrderDetail(Long id) {
        Order order = orderApiRepository.findOne(id);
        if (order == null) {
            throw new DataNotFoundException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    /**
     * 订单删除
     * state   0:正常状态，1：删除状态
     *
     * @param id 订单id
     */
    @Override
    public void deleteOrder(Long id) {
        Order order = orderApiRepository.findOne(id);
        if (order == null) {
            throw new DataNotFoundException(ResultCode.ORDER_NOT_EXIST);
        }
        order.setState((short) 1);
        orderApiRepository.save(order);
    }

    /**
     * 更改订单状态
     *
     * @param id     订单id
     * @param status 订单状态
     * @return JsonResult，data为 OrderDto
     */
    @Override
    public Order updateOrderStatus(Long id, Short status) {
        Order order = orderApiRepository.findOne(id);
        if (order == null) {
            throw new DataNotFoundException(ResultCode.ORDER_NOT_EXIST);
        }
        // 其它操作无误后，修改订单状态
        order.setOrderStatus(status);
        order = orderApiRepository.save(order);

        return order;
    }

    @Transactional
    @Override
    public Order createJDGiftOrder(OrderInfoVo orderInfoVo) {
        Order order = createOrder(orderInfoVo);
        createJDGiftProductOrder(order);
        return order;
    }

    /**
     * 自动创建大礼盒对应的京东商品订单
     *
     * @param order
     */
    private void createJDGiftProductOrder(Order order) {
        String skus = order.getGiftSkus();
        log.info("createJDGiftProductOrder,order={}", JSON.toJSONString(order));
        List<ProductDTO> productList = productFacade.getProductListBySkus(skus);
        //查询商品系统，组装参数
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setProvinceId(order.getProvinceId());
        orderInfoVo.setCityId(order.getCityId());
        orderInfoVo.setCountyId(order.getCountyId());
        orderInfoVo.setTownId(order.getTownId());
        orderInfoVo.setDetail(order.getDetail());
        orderInfoVo.setSplicingAddress(order.getAddress());
        orderInfoVo.setName(order.getName());
        orderInfoVo.setPhone(order.getPhone());
        orderInfoVo.setFreight(0);
        orderInfoVo.setRemark("");
        orderInfoVo.setOrderFrom(Constant.ORDER_FROM_JD);


        //订单裸价(不包含运费)=所有的商品销售价(京东价/会员价)*对应数量之和
        BigDecimal orderNakedPrice = BigDecimal.ZERO;

        List<OrderItemVo> orderItemVos = new ArrayList<>();
        for (ProductDTO item : productList) {
            OrderItemVo vo = new OrderItemVo();
            vo.setSkuId(item.getSku());
            vo.setNum(1);
            BigDecimal jdPrice = new BigDecimal(item.getJdPrice());
            BigDecimal salePrice = jdPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            vo.setSalePrice(salePrice);
            orderNakedPrice = orderNakedPrice.add(salePrice.multiply(new BigDecimal(vo.getNum()))).setScale(2, BigDecimal.ROUND_HALF_UP);

            vo.setFloorPrice(new BigDecimal(item.getPurchasePrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
            vo.setPlatformPrice(new BigDecimal(item.getJdPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
            vo.setMemberPrice(new BigDecimal(item.getMemberPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
            vo.setProductName(item.getName());
            vo.setProductPicture(item.getImgPath());
            vo.setProductType(2);
            orderItemVos.add(vo);
        }
        orderInfoVo.setOrderItemVos(orderItemVos);

        orderInfoVo.setOrderNakedPrice(orderNakedPrice);

        log.info("createJDGiftProductOrder,orderInfoVo={}", JSON.toJSONString(orderInfoVo));

        //手动创建京东订单
        Order productOrder = createOrder(orderInfoVo);
        Long id = productOrder.getId();
        // 更新京东订单支付金额为9999，防止用户去待付款支付
        orderApiRepository.updatePayPrice(new BigDecimal("99999"), id);
        // 更新京东大礼盒订单的关联生成的京东商品订单ID
        orderApiRepository.updateJdOrderId(id, order.getId());
        log.info("createJDGiftProductOrder,orderNum={},newOrderNum={}", order.getOrderNum(), productOrder.getOrderNum());
    }

    /**
     * 创建订单
     *
     * @param orderInfoVo 下单信息
     * @return Order
     */
    @Transactional
    @Override
    public Order createOrder(OrderInfoVo orderInfoVo) {
        User currentUser = identityServer.getCurrentUser();
        Short orderFrom = orderInfoVo.getOrderFrom();
        // 计算订单项中价格是否正确
        Order order = createOrderDefaultParams(currentUser, orderInfoVo);

        // 生成订单号
        String orderNum = CommonUtil.createOrderNum(OrderPreFixEnum.GIFT, currentUser.getPhone());

        // 判断订单是否是 jd 订单
        switch (orderFrom) {
            case Constant.ORDER_FROM_GIFT:
                order.setOrderNum(orderNum);

                countingGiftOrder(currentUser, orderInfoVo, order);
                // 处理大礼包等操作
                dealOrderFromSonnhe(order);
                break;
            case Constant.ORDER_FROM_JD:
                // 生成订单号
                orderNum = CommonUtil.createOrderNum(OrderPreFixEnum.JD_GOODS, currentUser.getPhone());
                order.setOrderNum(orderNum);

                if (StringUtils.isNotBlank(order.getShareProfitFcode())) {
                    //分享赚商品会带上fcode
                    User userByFcode = this.userApiRepository.findUserByFcode(order.getShareProfitFcode());
                    if (userByFcode == null) {
                        throw new BusinessException("分享人fcode不存在");
                    }
                }

                validPrice(currentUser, orderInfoVo);
                // 检查库存
                if (!checkStock(orderInfoVo, order)) {
                    return order;
                }
                // 计算 jd 订单
                countingJDOrder(currentUser, orderInfoVo, order);
                // 处理 jd 订单
                dealOrderFromJD(orderInfoVo, order);
                break;
            default:
                break;
        }

        // 保存订单
        order = orderApiRepository.save(order);

        // 开启延时任务，超时未付款，自动取消订单，恢复库存
        // FIXME 程序down掉后，未付款订单将无法失效
        putInOrderTimer(order.getOrderNum(), order.getId());

        return order;
    }

    private boolean checkStock(OrderInfoVo orderInfoVo, Order order) {
        JDStockParam stockParam = new JDStockParam();
        stockParam.setCityId(orderInfoVo.getCityId());
        stockParam.setCountyId(orderInfoVo.getCountyId());
        stockParam.setProvinceId(orderInfoVo.getProvinceId());
        stockParam.setTownId(orderInfoVo.getTownId());

        List<OrderItemVo> orderItemVos = orderInfoVo.getOrderItemVos();
        Map<String, Integer> ids = new HashMap<>(orderItemVos.size());
        orderItemVos.forEach(orderItemVo -> ids.put(orderItemVo.getSkuId().toString(), orderItemVo.getNum()));
        stockParam.setProductIds(ids);

        // 请求商品系统检查库存
        Result<List<JDStockStateDTO>> listResult = jdStockFacade.checkJDProductStockAndLocalState(stockParam);
        List<JDStockStateDTO> stockResults = listResult.getData();
        if (CollectionUtils.isEmpty(stockResults)) {
            return true;
        }
        List<Long> noStockSku = new ArrayList<>();
        List<Long> invalidSku = new ArrayList<>();
        List<Long> unSaleSku = new ArrayList<>();
        stockResults.forEach(stockResult -> {
            if (Constant.PRODUCT_INVALID.equals(stockResult.getStockStateId())) {
                //商品下架
                invalidSku.add(stockResult.getSkuId());
            } else if (Constant.STOCK_NOT_ENOUGH.equals(stockResult.getStockStateId())) {
                //无库存
                noStockSku.add(stockResult.getSkuId());
            } else if (Constant.PRODUCT_UN_SALE.equals(stockResult.getStockStateId())) {
                unSaleSku.add(stockResult.getSkuId());
            }
        });

        // 无库存
        order.setNoStockSku(noStockSku);
        // 无效
        order.setInvalidSku(invalidSku);
        // 不可售
        order.setUnSaleSku(unSaleSku);
        //都为空代表校验通过
        return invalidSku.isEmpty() && noStockSku.isEmpty() && unSaleSku.isEmpty();

    }


    /**
     * 查询运费
     */
    @Override
    public FreightResultTo getFreight(FreightVo freightVo) {
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_FREIGHT, freightVo, JDResult.class);
        return JDUtil.dealRequestData(entity, FreightResultTo.class, false);
    }

    /**
     * 查询是否支持售后
     */
    @Override
    public QueryAfterSaleResultTo isSupportAfterSale(QueryAfterSaleVo queryAfterSaleVo) {
        OrderItemByJdIdBo orderItemByJdIdBo = orderApiRepository.getInfoByJdOrderAndSku(queryAfterSaleVo.getJdOrderId(), queryAfterSaleVo.getSkuId());
        if (orderItemByJdIdBo == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        // 1. 根据 jdOrderId 查询订单详情信息，迭代解析 skuId 是否存在拆单情况，存在，则获取其子单 jdOrderId
        queryAfterSaleVo.setJdOrderId(dealDemolitionOrder(queryAfterSaleVo.getJdOrderId(), queryAfterSaleVo.getSkuId(), restTemplate));
        // 2. 根据 jdOrderId 查询是否支持售后
        HttpEntity<MultiValueMap> httpEntity;
        try {
            httpEntity = encapsulateFormData(queryAfterSaleVo);
        } catch (IllegalAccessException e) {
            throw new BusinessException("数据封装出错");
        }
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_IS_SUPPORT_AFTER_SALE, httpEntity, JDResult.class);
        return JDUtil.dealRequestData(entity, QueryAfterSaleResultTo.class, false);
    }

    @Override
    public List<ProductAfterSaleTypeTo> getAfterSaleType(QueryAfterSaleVo queryAfterSaleVo) {
        OrderItemByJdIdBo orderItemByJdIdBo = orderApiRepository.getInfoByJdOrderAndSku(queryAfterSaleVo.getJdOrderId(), queryAfterSaleVo.getSkuId());
        if (orderItemByJdIdBo == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        queryAfterSaleVo.setJdOrderId(dealDemolitionOrder(queryAfterSaleVo.getJdOrderId(), queryAfterSaleVo.getSkuId(), restTemplate));
        HttpEntity<MultiValueMap> httpEntity;
        try {
            httpEntity = encapsulateFormData(queryAfterSaleVo);
        } catch (IllegalAccessException e) {
            throw new BusinessException("数据封装出错");
        }
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_PRODUCT_AFTER_SALE_TYPE, httpEntity, JDResult.class);
        ParseAfterSaleType parseAfterSaleType = JDUtil.dealRequestData(entity, ParseAfterSaleType.class, true);
        return wrapSaleTypes(parseAfterSaleType.getList());
    }

    /**
     * 提供当前售后类型支不支持的状态  前端用于禁用操作
     *
     * @param typeTos
     * @return
     */
    private List<ProductAfterSaleTypeTo> wrapSaleTypes(List<ProductAfterSaleTypeTo> typeTos) {
        if (CollectionUtils.isEmpty(typeTos)) {
            return SALE_TYPES;
        }
        List<ProductAfterSaleTypeTo> resultTypes = new ArrayList<>(SALE_TYPES);
        for (ProductAfterSaleTypeTo saleType : typeTos) {
            if (SALE_TYPES.contains(saleType)) {
                saleType.setSupport(true);
                resultTypes.remove(saleType);
                resultTypes.add(saleType);
            }

        }
        return resultTypes;
    }

    /**
     * 申请售后
     */
    @Transactional
    @Override
    public Boolean applyAfterSale(ApplyAfterSaleVo applyAfterSaleVo) {
        if (this.orderApiRepository.findOrderByJdOrderId(Long.valueOf(applyAfterSaleVo.getJdOrderId())).getPayPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("不支持售后");
        }

        log.info("applyAfterSaleVo = [{}]", JSON.toJSONString(applyAfterSaleVo));
        dealNoReturnware(applyAfterSaleVo.getAsPickwareDto());
        log.info("dealNoReturnware applyAfterSaleVo = [{}]", JSON.toJSONString(applyAfterSaleVo));
        Map<String, String> skuMap = applyAfterSaleVo.getAsDetailDto();
        String skuId = skuMap.get("skuId");
        // 父jd订单id
        String pJdOrderId = applyAfterSaleVo.getJdOrderId();
        // 查询是否存在改订单项
        OrderItemByJdIdBo orderItemByJdIdBo = orderApiRepository.getInfoByJdOrderAndSku(pJdOrderId, Long.parseLong(skuId));
        if (orderItemByJdIdBo == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        // 1. 根据 jdOrderId 查询订单详情信息，迭代解析 skuId 是否存在拆单情况，存在，则获取其子单 jdOrderId
        applyAfterSaleVo.setJdOrderId(dealDemolitionOrder(pJdOrderId, Long.parseLong(skuId), restTemplate));
        log.info("cid = [{}]", applyAfterSaleVo.getJdOrderId());
        log.info("request param = [{}]", applyAfterSaleVo);
        // 2. 根据 jdOrderId 进行售后申请


        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_APPLY_AFTER_SALE, applyAfterSaleVo, JDResult.class);
        // 查询单子并进行保存
        updateAfterSaleStateByItemId(orderItemByJdIdBo.getOrderItemId(), applyAfterSaleVo.getJdOrderId());
        return JDUtil.dealRequestData(entity, Boolean.class, false);
    }

    /**
     * 处理售后反件信息为空
     *
     * @param asReturnwareDto
     */
    private void dealNoReturnware(Map<String, String> asReturnwareDto) {
        boolean isProvinceEmpty = asReturnwareDto.get("pickwareProvince") == null || Integer.valueOf(asReturnwareDto.get("pickwareProvince")) == 0;
        if (isProvinceEmpty) {
            // 查询默认地址
            UserAddress userAddress = addressApiService.getDefaultAddress();
            if (userAddress == null) {
                throw new BusinessException(ResultCode.USERADDRESS_NOT_EXIST);
            }
            String addrIds = userAddress.getAddressNum();
            if (StringUtils.isEmpty(addrIds)) {
                throw new BusinessException(ResultCode.USERADDRESS_NOT_EXIST);
            }
            String[] arr = addrIds.split(",");
            asReturnwareDto.put("pickwareProvince", arr[0]);
            asReturnwareDto.put("pickwareCity", arr[1]);
            asReturnwareDto.put("pickwareCounty", arr[2]);
            asReturnwareDto.put("pickwareVillage", arr[3]);
            asReturnwareDto.put("pickwareAddress", userAddress.getAddressDetail());
        }
    }

    /**
     * 取消售后
     */
    @Override
    public Boolean cancelAfterSale(CancelAfterSaleVo cancelAfterSaleVo) {
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_CANCEL_AFTER_SALE, cancelAfterSaleVo, JDResult.class);
        return JDUtil.dealRequestData(entity, Boolean.class, false);
    }

    @Override
    public List<AfterSale> getAfterSaleInfo(Long orderItemId) {
        return updateAfterSaleStateByItemId(orderItemId, null);
    }

    /**
     * 订单跟踪
     * 注：form-data格式
     *
     * @param trackVo 订单跟踪参数
     */
    @Override
    public TrackResultTo trackOrder(TrackVo trackVo) {
        HttpEntity<MultiValueMap> httpEntity;
        try {
            httpEntity = encapsulateFormData(trackVo);
        } catch (IllegalAccessException e) {
            throw new BusinessException("数据封装出错");
        }
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_ORDER_TRACK, httpEntity, JDResult.class);
        return JDUtil.dealRequestData(entity, TrackResultTo.class, false);
    }

    @Override
    public OrderTraceDTO splitTrackOrder(TrackVo trackVo) {
        try {
            OrderTraceDTO result = new OrderTraceDTO();
            Long jdOrderId = trackVo.getJdOrderId();

            String url = URLConstant.JD_ORDER_DETAIL + "&tradeNo={tradeNo}";
            ResponseEntity<JDResult> entity = restTemplate.getForEntity(url, JDResult.class, jdOrderId);
            OrderDetailTo orderDetailTo = JDUtil.dealRequestData(entity, OrderDetailTo.class, false);
            if (orderDetailTo.getCOrder() == null || orderDetailTo.getCOrder().isEmpty()) {
                //没有拆单的物流查询后直接返回
                TrackResultTo trackResultTo = trackOrder(trackVo);
                result.setSimpleOrder(trackResultTo);
                return result;
            }

            String orderNum = orderApiRepository.getOrderNumByJdOrderId(trackVo.getJdOrderId());
            SplitOrderDTO dto = new SplitOrderDTO();
            dto.setOrderNum(orderNum);

            List<SplitOrderDetailDTO> orderList = new ArrayList<>();

            List<Long> allSkuId = new ArrayList<>();

            List<COrderDetailTo> list = orderDetailTo.getCOrder();
            for (COrderDetailTo item : list) {
                SplitOrderDetailDTO orderDetail = new SplitOrderDetailDTO();

                Long jdOrderId1 = item.getJdOrderId();
                orderDetail.setJdOrderId(jdOrderId1);

                List<SplitOrderDetaiSkuslDTO> skus = new ArrayList<>();
                List<OrderSkuTo> skuList = item.getSku();
                for (OrderSkuTo sku : skuList) {
                    SplitOrderDetaiSkuslDTO detailSku = new SplitOrderDetaiSkuslDTO();
                    Long skuId = sku.getSkuId();
                    detailSku.setSku(skuId);
                    skus.add(detailSku);
                    allSkuId.add(skuId);
                }
                orderDetail.setSkus(skus);
                orderList.add(orderDetail);
            }
            dto.setOrderList(orderList);

            Map<Long, SplitOrderDetaiSkuslDTO> map = getSkuInfo(allSkuId);

            for (SplitOrderDetailDTO orderDto : orderList) {
                List<SplitOrderDetaiSkuslDTO> skus = orderDto.getSkus();
                for (SplitOrderDetaiSkuslDTO detailSku : skus) {
                    Long sku = detailSku.getSku();
                    SplitOrderDetaiSkuslDTO splitOrderDetaiSkuslDTO = map.get(sku);
                    detailSku.setName(splitOrderDetaiSkuslDTO.getName());
                    detailSku.setImgPath(splitOrderDetaiSkuslDTO.getImgPath());
                }
            }
            result.setSplitOrder(dto);
            return result;
        } catch (Exception e) {
            log.error("splitTrackOrder", e);
        }
        return null;
    }

    /**
     * 去商品系统批量查询商品信息
     */
    private Map<Long, SplitOrderDetaiSkuslDTO> getSkuInfo(List<Long> skuList) {
        Map<Long, SplitOrderDetaiSkuslDTO> map = new HashMap<>();

        StringBuffer sb = new StringBuffer();
        // 封装查询参数，并保存迭代查询引用
        skuList.forEach(i -> {
            sb.append(i).append(",");
        });
        String skus = sb.substring(0, sb.length() - 1);
        // 请求产品系统
        List<ProductDTO> list = productFacade.getProductListBySkus(skus);
        // 封装解析产品
        for (ProductDTO productDTO : list) {
            SplitOrderDetaiSkuslDTO item = new SplitOrderDetaiSkuslDTO();
            Long sku = productDTO.getSku();
            item.setSku(sku);
            item.setImgPath(productDTO.getImgPath());
            item.setName(productDTO.getName());
            map.put(sku, item);
        }
        return map;
    }

    /**
     * 需要对生成付款订单进行缓存，若付款失败退出后将无法再次付款
     * 测试 openId ：or6o71ccIhdy69uZsuXc7itVWTos
     *
     * @param payOrderVo 生成订单支付参数
     */
    @Transactional
    @Override
    public PaymentOrderDto payOrder(PayOrderVo payOrderVo) {

        // 首次进入，需先查询缓存是否有已经生成的未付款订单。

        String wxOpenId = payOrderVo.getWxOpenId();
        // 查询订单
        String orderNum = payOrderVo.getOrderNum();
        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + orderNum;
        Number number = (Number) redisUtils.get(orderNotPayKey);
        if (number == null) {
            // 订单号错误或过期
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        long orderId = number.longValue();
        Order order = orderApiRepository.findOne(orderId);
        // 校验用户是否重复下单大礼包购买
        if (Constant.ORDER_FROM_GIFT == order.getOrderFrom()) {
            checkingRepeatBuyGift(order.getUser().getUid());
        }

        // 获取支付信息
        String payInfoKey = RedisConstant.ORDER_WX_PAY_INFO_PREFIX + orderNum;
        PaymentOrderDto paymentOrderDto = (PaymentOrderDto) redisUtils.get(payInfoKey);
        if (paymentOrderDto == null) {

            //更新支付渠道
            orderApiRepository.updatePayChannel(PayChannelEnum.WX_H5_PAY.getCode(), orderId);

//            int payPrice = order.getPayPrice().movePointRight(2).intValue();
//            if (!Constant.isRealPay) {
//                payPrice = 1;
//            }
//
//            WXPayParam wxparam = new WXPayParam();
//            wxparam.setTradeNo(orderNum);
//            wxparam.setPayChannel(PayChannelEnum.WX_H5_PAY.getCode());
//            wxparam.setTotalFee(payPrice);
//            String ip = RequestContextHolderUtil.getRequestIp();
//            wxparam.setSpbillCreateIp(ip);
//            wxparam.setOpenId(wxOpenId);
//            wxparam.setBody("东东优汇产品");
//            try {
//                com.ddyh.commons.result.Result<WXH5PayDTO> result = payFacade.getRequest(wxparam);
//                if (result.getCode().equals(com.ddyh.commons.result.ResultCode.SUCCESS.getCode())) {
//                    WXH5PayDTO data = result.getData();
//                    paymentOrderDto = new PaymentOrderDto();
//                    paymentOrderDto.setAppId(data.getAppId());
//                    paymentOrderDto.setPaySign(data.getPaySign());
//                    paymentOrderDto.setNonceStr(data.getNonceStr());
//                    paymentOrderDto.setPrepayId(data.getPrepayId());
//                    paymentOrderDto.setTimeStamp(data.getTimeStamp());
//                    paymentOrderDto.setSignType(data.getSignType());
//                }
//            } catch (com.ddyh.commons.exception.BusinessException e) {
//                e.printStackTrace();
//            }


            // 拼接wx统一下单接口，并拼接为xml格式参数
            String param = WXUtils.splicingUnifiedOrderParam(order.getPayPrice(), order.getOrderNum(), wxOpenId);
            // 获取统一下单id
            String prepayId = WXUtils.getPrepayId(restTemplate, param);
            paymentOrderDto = WXUtils.splicingPaymentOrderDto(prepayId);


            // 缓存支付信息
            long expireTime = Constant.ORDER_TIMER_EFFECTIVE + 2;       // 延迟于订单过期2分钟
            redisUtils.set(payInfoKey, paymentOrderDto, expireTime, TimeUnit.MINUTES);
        }
        return paymentOrderDto;
    }

    @Override
    public WxAppPaymentOrderDto appWxPayOrder(WxAppPayOrderVo wxAppPayOrderVo) {
        // 首次进入，需先查询缓存是否有已经生成的未付款订单。
        // 查询订单
        String orderNum = wxAppPayOrderVo.getOrderNum();
        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + orderNum;
        Number number = (Number) redisUtils.get(orderNotPayKey);
        if (number == null) {
            // 订单号错误或过期
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        long orderId = number.longValue();
        Order order = orderApiRepository.findOne(orderId);
        // 校验用户是否重复下单大礼包购买
        if (Constant.ORDER_FROM_GIFT == order.getOrderFrom()) {
            checkingRepeatBuyGift(order.getUser().getUid());
        }
        // 获取支付信息
        String payInfoKey = RedisConstant.ORDER_WX_PAY_INFO_PREFIX + orderNum;
        WxAppPaymentOrderDto wxAppPaymentOrderDto = (WxAppPaymentOrderDto) redisUtils.get(payInfoKey);
        if (wxAppPaymentOrderDto == null) {

            //更新支付渠道
            orderApiRepository.updatePayChannel(PayChannelEnum.WX_APP_PAY.getCode(), orderId);

            // 拼接wx统一下单接口，并拼接为xml格式参数
            String param = WXUtils.splicingAppUnifiedOrderParam(order.getPayPrice(), orderNum);
            // 获取统一下单id
            String prepayId = WXUtils.getPrepayId(restTemplate, param);
            wxAppPaymentOrderDto = WXUtils.splicingWxAppPaymentOrderDto(prepayId);


//            int payPrice = order.getPayPrice().movePointRight(2).intValue();
//            if (!Constant.isRealPay) {
//                payPrice = 1;
//            }
//
//            WXPayParam wxparam = new WXPayParam();
//            wxparam.setTradeNo(orderNum);
//            wxparam.setPayChannel(PayChannelEnum.WX_APP_PAY.getCode());
//            wxparam.setTotalFee(payPrice);
//            String ip = RequestContextHolderUtil.getRequestIp();
//            wxparam.setSpbillCreateIp(ip);
//            wxparam.setBody("东东优汇产品");
//            try {
//                com.ddyh.commons.result.Result<WXAppPayDTO> result = payFacade.getRequest(wxparam);
//                if (result.getCode().equals(com.ddyh.commons.result.ResultCode.SUCCESS.getCode())) {
//                    WXAppPayDTO data = result.getData();
//                    wxAppPaymentOrderDto = new WxAppPaymentOrderDto();
//                    wxAppPaymentOrderDto.setAppid(data.getAppid());
//                    wxAppPaymentOrderDto.setSign(data.getSign());
//                    wxAppPaymentOrderDto.setNoncestr(data.getNoncestr());
//                    wxAppPaymentOrderDto.setPartnerid(data.getPartnerid());
//                    wxAppPaymentOrderDto.setPrepayid(data.getPrepayid());
//                    wxAppPaymentOrderDto.setTimestamp(data.getTimestamp());
//                    wxAppPaymentOrderDto.setSignType(data.getSignType());
//                }
//            } catch (com.ddyh.commons.exception.BusinessException e) {
//                e.printStackTrace();
//            }


            // 缓存支付信息
            long expireTime = Constant.ORDER_TIMER_EFFECTIVE + 2;       // 延迟于订单过期2分钟
            redisUtils.set(payInfoKey, wxAppPaymentOrderDto, expireTime, TimeUnit.MINUTES);
        }
        return wxAppPaymentOrderDto;
    }

    @Transactional
    @Override
    public void payOrderCallBack(String orderNum) {
        Order order = orderApiRepository.findOrderByOrderNum(orderNum);
        if (order == null) {
            throw new DataNotFoundException(ResultCode.ORDER_NOT_EXIST);
        }
        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + orderNum;
        boolean exists = redisUtils.exists(orderNotPayKey);

        if (exists) {
            // 更改订单状态
            order.setOrderStatus(Constant.ORDER_STATUS_PAY);
            order.setPayTime(new Date());
            order = orderApiRepository.save(order);
            // 删除缓存
            redisUtils.remove(orderNotPayKey);
            // 根据订单类型调用不同的回调接口
            if (order.getOrderFrom() == Constant.ORDER_FROM_GIFT) {
                payMemberOrderCallBack(order);
                if (StringUtils.isNotBlank(order.getGiftSkus())) {
                    //处理京东大礼盒订单 TODO 后续改成订单类型为4，重新设计
                    processJDGiftProductOrder(order);
                }
            } else {
                payJdOrderCallBack(order);
            }
        } else {
            int res = orderApiRepository.payOrderStatusByOrderNum(orderNum, Constant.ORDER_STATUS_CANCELED);
            if (res > 0) {
                log.error("订单过期，但是支付成功，订单id为 [{}]", order.getId());
            }
        }
    }


    private void processJDGiftProductOrder(Order order) {
        log.info("processJDGiftProductOrder,order={}", JSON.toJSONString(order));

        Long jdOrderId = order.getJdOrderId();
        //京东大礼盒订单的关联生成的京东商品订单ID
        Order productOrder = orderApiRepository.getOne(jdOrderId);

        // 更新京东订单支付金额为0
        // 更改订单状态已支付
        productOrder.setOrderStatus(Constant.ORDER_STATUS_PAY);
        productOrder.setPayTime(new Date());
        productOrder.setPayPrice(BigDecimal.ZERO);
        productOrder = orderApiRepository.save(productOrder);

        //通知京东订单已支付
        notifyJd(productOrder);

        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + productOrder.getOrderNum();
        boolean exists = redisUtils.exists(orderNotPayKey);
        if (exists) {
            // 删除缓存
            redisUtils.remove(orderNotPayKey);
        }

        log.info("processJDGiftProductOrder,end={}", JSON.toJSONString(productOrder));
    }

    @Override
    public void repurchase(Long id) {
        Order order = orderApiRepository.findOne(id);
        if (order == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        User currentUser = identityServer.getCurrentUser();
        Long uid = currentUser.getUid();
        if (!uid.equals(order.getUser().getUid())) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() < 1) {
            return;
        }
        List<RepurchaseBo> repurchaseBos = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            repurchaseBos.add(CommonUtil.dataHandler(orderItem, new RepurchaseBo()));
        }
        shoppingCartService.addItemsIntoCart(repurchaseBos);
    }

    /**
     * jd支付成功回调
     *
     * @param order
     */
    private void payJdOrderCallBack(Order order) {
        rebate(order);

        //通知京东订单已支付
        notifyJd(order);
    }

    private void rebate(Order order) {
        /**
         * 1.跟场景和购买者用户类型有关：
         *     1）购买者是京卡：按之前返利逻辑走；
         *     2）购买者是体验卡且商品是分享赚：
         *           如果是APP购买，按体验卡走
         *           如果是H5购买，并且分享者是京卡会员，走分享赚，不是京卡会员直接不返利
         *     3）购买者是体验卡：直接按体验卡返利
         *     4）商品是分享赚：(相当于购买者是普通用户)
         *            如果分享者是京卡会员，直接按分享赚返利
         * 	          如果分享者不是京卡会员(可能这时候分享者已经过期)，按之前返利逻辑走
         *     5）购买者是普通用户：按之前返利逻辑走；
         * 2.场景的判断：
         *     通过最后支付渠道来判断app支付还是h5支付；
         */
        if (order.getUser().getCharacterType() > Constant.USER_NORMAL_TYPE) {
            //京卡
            // 进行之前返利逻辑
            normalRebate(encapsulateOrderInRebateBo(order));
        } else if (order.getUser().getExpCardType() != null && order.getUser().getExpCardType() > 0 && StringUtils.isNotBlank(order.getShareProfitFcode())) {
            //体验卡+分享赚 同时存在的情况
            if (PayChannelEnum.WX_H5_PAY.getCode().equals(order.getPayChannel())) {
                //h5支付 走分享赚返利
                //并且分享者是京卡会员，才能返利，否则不返利
                User puser = userApiRepository.findUserByFcode(order.getShareProfitFcode());
                if (puser.getCharacterType() > Constant.USER_NORMAL_TYPE) {
                    shareProfitRebate(order);
                }
            } else {
                //app支付 走体验卡返利
                expCardRebate(order);
            }
        } else if (order.getUser().getExpCardType() != null && order.getUser().getExpCardType() > 0) {
            //体验卡
            expCardRebate(order);
        } else if (StringUtils.isNotBlank(order.getShareProfitFcode())) {
            User puser = userApiRepository.findUserByFcode(order.getShareProfitFcode());
            if (puser.getCharacterType() > Constant.USER_NORMAL_TYPE) {
                //如果是分享者是京卡会员，走分享赚
                shareProfitRebate(order);
            } else {
                //如果是分享者不是京卡会员，可能是过期，进行之前返利逻辑
                normalRebate(encapsulateOrderInRebateBo(order));
            }

        } else {
            // 进行之前返利逻辑
            normalRebate(encapsulateOrderInRebateBo(order));
        }
    }

    private void notifyJd(Order order) {
        String uidStr = order.getUser().getUid().toString();
        // 将jd订单号放入未完成订单队列中
        Object o = redisUtils.hmGet(RedisConstant.ORDER_IN_THE_ROUGH_HASH, uidStr);
        JSONObject jsonObject;
        if (o == null) {
            jsonObject = new JSONObject();
        } else {
            String jsonStr = JSON.toJSONString(o);
            jsonObject = JSONObject.parseObject(jsonStr);
        }
        jsonObject.put(order.getJdOrderId().toString(), 0);
        redisUtils.hmSet(RedisConstant.ORDER_IN_THE_ROUGH_HASH, uidStr, jsonObject);

        if (IS_TEST) {
            return;
        }
        // 通知环球，支付成功
        try {
            OrderPayNotifyJdRetryLogListener listener = new OrderPayNotifyJdRetryLogListener(redisUtils, eventBus, order.getId());
            Retryer hqRetryer = retryBuilder.build(RemoteAccessException.class, null, 5, TimeUnit.SECONDS, 5, listener);
            hqRetryer.call(new NotifyHqPaySuccess(order, restTemplate));
        } catch (Exception e) {
            log.error("payJdOrderCallBack NotifyHqPaySuccess orderId:" + order.getId() + ", error: {}", e);
        }
    }

    /**
     * 体验卡返利
     *
     * @param order
     */
    private void expCardRebate(Order order) {
        BigDecimal rewardAmount = new BigDecimal("0");
        //基数 = 成交价-供货价-（成交价-供货价）*13%-成交价*0.6%
        //返利金额 = 基数 * 50%
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            //成交价
            BigDecimal salePrice = orderItem.getSalePrice();
            //供货价
            BigDecimal floorPrice = orderItem.getFloorPrice();

            BigDecimal divide = salePrice.divide(floorPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = divide.multiply(new BigDecimal("0.13")).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply2 = divide.multiply(new BigDecimal("0.006")).setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal price = divide.divide(multiply).divide(multiply2).setScale(2, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(new BigDecimal("0.5")).setScale(2, BigDecimal.ROUND_HALF_UP);
            //循环累计返利
            rewardAmount = rewardAmount.add(price);
        }
        if (rewardAmount.compareTo(new BigDecimal("0.01")) >= 0) {
            //大于一分钱返利
            String parentFcode = order.getUser().getParentFcode();
            User puser = userApiRepository.findUserByFcode(parentFcode);
            //真正返利时间一个月后
            Date accountEntryTime = CommonUtil.getAfterDate(Calendar.MONTH, 1, new Date());
            rebateApiService.saveRebate(order.getId(), puser.getUid(), order.getUser().getUid(), order.getUser().getPhone(), rewardAmount, Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY, (short) OrderTypeEnum.JD_GOODS.getType(), order.getOrderNakedPrice(), accountEntryTime, Constant.USER_REBATE_TYPE_MEMBER, rewardAmount.movePointRight(2).intValue(), Constant.REBATE_TYPE_EXP_CARD_JD, rewardAmount, puser.getPhone());
        }
    }

    /**
     * 分享赚返利
     *
     * @param order
     */
    private void shareProfitRebate(Order order) {
        BigDecimal rewardAmount = new BigDecimal("0");
        //基数 = 成交价-供货价-（成交价-供货价）*13%-成交价*0.6%
        //返利金额 = 基数 * 50%
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            //成交价
            BigDecimal salePrice = orderItem.getSalePrice();
            //供货价
            BigDecimal floorPrice = orderItem.getFloorPrice();

            BigDecimal divide = salePrice.divide(floorPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply = divide.multiply(new BigDecimal("0.13")).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal multiply2 = divide.multiply(new BigDecimal("0.006")).setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal price = divide.divide(multiply).divide(multiply2).setScale(2, BigDecimal.ROUND_HALF_UP);
            price = price.multiply(new BigDecimal("0.5")).setScale(2, BigDecimal.ROUND_HALF_UP);
            //循环累计返利
            rewardAmount = rewardAmount.add(price);
        }
        if (rewardAmount.compareTo(new BigDecimal("0.01")) >= 0) {
            String parentFcode = order.getShareProfitFcode();
            User puser = userApiRepository.findUserByFcode(parentFcode);

            //真正返利时间一个月后
            Date accountEntryTime = CommonUtil.getAfterDate(Calendar.MONTH, 1, new Date());
            rebateApiService.saveRebate(order.getId(), puser.getUid(), order.getUser().getUid(), order.getUser().getPhone(), rewardAmount, Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY, (short) OrderTypeEnum.JD_GOODS.getType(), order.getOrderNakedPrice(), accountEntryTime, Constant.USER_REBATE_TYPE_MEMBER, rewardAmount.movePointRight(2).intValue(), Constant.REBATE_TYPE_SHARE_PROFIT, rewardAmount, puser.getPhone());
        }
    }

    @Override
    public void updateRebateData(String orderNum) {

//        String str = "1861659599920190729143049,1867689283920190703093816,1867689283920190710035035,1867689283920190713031550,1867689283920190715103520,1867689283920190728045023,1502134219020190720132526,1880650166120190808072549,1862136907220190711232856,1899148826120190711081111,1802234635220190803130936,1357152543320190623110326,1357152543320190719000429,1357152543320190727034937,1328989675720190621084827,1357235827620190628232857,1357235827620190630080736,1357235827620190701131359,1389137128820190626090225,1389137128820190714062712,1815683088320190714132240,1826803347620190809041609,1868198686620190716125725,1386611160420190628155336,1386611160420190723162531,1866379563020190620140009,1866379563020190624055407,1866379563020190701124258,1562907277720190718070112,1829799308120190625060754,1829799308120190706165719,1832619539120190708040617,1315656413320190626104428,1896984315120190707072712,1300448069620190707110648,1366762793320190705135937,1397277051120190725020734,1381168065920190717090113,1381168065920190717091254,1872935011320190725155459,1598201587520190714235830,1598201587520190726004212,1377431763220190725085251,1861600653320190717131227,1861600653320190721025123,1305226681720190803161317,1336518851320190727091658,1894920249520190721021324,1396670250920190722061308,1350187333020190727131641,1561111071020190807102154,1770731554620190728151234,1350748767120190730053931,1595022099820190730053125,1768216352820190729093405,1768216352820190729104231,1591676641020190730073510,1812662802620190807171933,1862165846220190803161835,1838429598620190805134300,1893225611120190806082651,1348528216120190807102905,1377008436320190805084844,1325940961020190806020627";

//        String str = "1861659599920190729143049,1867689283920190703093816,1867689283920190710035035,1867689283920190713031550,1867689283920190715103520,1867689283920190728045023,1502134219020190720132526,1880650166120190808072549,1862136907220190711232856,1899148826120190711081111,1802234635220190803130936,1357152543320190623110326,1357152543320190719000429,1357152543320190727034937,1328989675720190621084827,1357235827620190628232857,1357235827620190630080736,1357235827620190701131359,1389137128820190626090225,1389137128820190714062712,1815683088320190714132240,1826803347620190809041609,1868198686620190716125725,1386611160420190628155336,1386611160420190723162531,1866379563020190620140009,1866379563020190624055407,1866379563020190701124258,1562907277720190718070112,1829799308120190625060754,1829799308120190706165719,1832619539120190708040617,1315656413320190626104428,1896984315120190707072712,1300448069620190707110648,1366762793320190705135937,1397277051120190725020734,1381168065920190717090113,1381168065920190717091254,1872935011320190725155459,1598201587520190714235830,1598201587520190726004212,1377431763220190725085251,1861600653320190717131227,1861600653320190721025123,1305226681720190803161317,1336518851320190727091658,1894920249520190721021324,1396670250920190722061308,1350187333020190727131641,1561111071020190807102154,1770731554620190728151234,1350748767120190730053931,1595022099820190730053125,1768216352820190729093405,1768216352820190729104231,1591676641020190730073510,1812662802620190807171933,1862165846220190803161835,1838429598620190805134300,1893225611120190806082651,1348528216120190807102905,1377008436320190805084844,1325940961020190806020627";
        String str = "1861659599920190704164452,1861659599920190720074009,1861659599920190729143540,1858086634220190626064942,1858086634220190706000533,1858086634220190709074802,1858086634220190716073928,1858086634220190722155937,1785586147720190727005707,1867689283920190709015921,1867689283920190710025950,1880650166120190808081008,1380510291820190619232814,1871030339120190621135300,1871030339120190628110608,1871030339120190709112040,1871030339120190709112934,1357152543320190710123855,1328989675720190702101540,1879232869120190728095414,1381033607520190705124454,1396668693520190624101448,1396668693520190701135139,1826803347620190730054319,1580718331820190628132954,1386611160420190628155504,1527185563520190715085224,1396815696420190622061709,1396815696420190717065439,1396815696420190718045723,1396815696420190722034025,1396815696420190725053750,1866379563020190621085204,1302719855520190715035038,1829799308120190628082200,1829799308120190708025611,1832619539120190625022547,1360141098920190622085349,1360141098920190622090838,1360141098920190728091841,1360141098920190802113230,1315656413320190626113348,1315656413320190807011831,1850000000120190706055904,1850000000120190726045835,1801088390020190727024913,1555516303020190621095842,1555516303020190622063120,1555516303020190622071250,1555516303020190724064553,1300448069620190707110217,1300448069620190725081628,1898252177020190715160825,1385596791120190624144217,1379526485820190808010009,1390173415520190724015204,1358550016220190722051047,1366762793320190712122614,1366762793320190802021539,1911238881520190723040956,1397277051120190704134920,1397277051120190717040224,1397277051120190719045941,1368835498020190705083242,1368835498020190705084822,1368835498020190716022726,1368835498020190729095117,1529131315220190801070611,1872935011320190709133021,1851831504520190806133220,1851831504520190806133539,1851831504520190806134951,1850017845420190806102301,1355204251620190725050852,1598201587520190808070110,1530723711120190718021402,1377431763220190808080626,1861600653320190805131519,1580799799720190719050113,1336518851320190726042818,1336518851320190727143918,1336518851320190729074325,1806618693920190803140453,1805324080920190724085340,1539512766120190723133656,1866201087720190801100932,1551558828520190808091540,1390748273820190727095936,1350748767120190730051913,1350748767120190730052605,1377005500820190802080146,1377005500820190802080409,1595022099820190731083023,1595022099820190804091205,1595022099820190806075631,1838429598620190804085957,1870622128820190805084243,1325940961020190805103427";
        for (String orderNum2 : str.split(",")) {
            Order order = orderApiRepository.findOrderByOrderNum(orderNum2);
            // 进行返利操作
            normalRebate(encapsulateOrderInRebateBo(order));
        }

    }

    /**
     * 大礼包购买支付成功
     */
    private void payMemberOrderCallBack(Order order) {
        // 判断是否存在会员商品
        List<OrderItem> orderItems = order.getOrderItems();
        OrderItem memberOrderItem = null;
        BigDecimal originPrice = new BigDecimal(0);
        for (OrderItem orderItem : orderItems) {
            originPrice = originPrice.add(orderItem.getMemberPrice().multiply(new BigDecimal(orderItem.getNum())));
            if (orderItem.getProductType() == Constant.PRODUCT_TYPE_MEMBER) {
                memberOrderItem = orderItem;
            }
        }

        if (memberOrderItem == null) {
            throw new BusinessException("非会员产品订单");
        }

        // TODO : 用更好的方法替代下面所有的saveAndFlush(用save方法会导致数据不及时更新， 其他查询无法查到最新数据)， 达到事务立即生效效果

        //更新上级、上级身份和下级数量
        updateParentFcode(order);
        // 升级会员
        upgradeMember(order);
        // 判断团购中是否存在该用户的绑定
        if (!consumeTeamBuy(order.getUser())) {
            // 返利
            normalRebate(encapsulateOrderInRebateBo(order));
        }
        //升级身份
        Long pid = updateMember(order.getUser().getUid());
        // 会员下级统计
        memberTeamService.countMember(pid);

        // 新增操作日志 观察user数据变化
        try {
            saveLog(order.getUser().getUid());
        } catch (Exception e) {
            log.info("payMemberOrderCallBack saveLog error: {}", e);
        }
    }

    private void saveLog(Long uid) {
        User user = userApiRepository.findOne(uid);
        OperaLog log = new OperaLog();
        log.setType("SEE_USER_CHANGE");
        log.setParams("uid: [" + uid + "]|parentFcode: [" + user.getParentFcode() + "]|channelId: [" + user.getChannelId() + "]|characterType: [" + user.getCharacterType() + "]");
        operaLogRepository.save(log);
    }

    private Long updateMember(Long uid) {
        Long pid;
        // 1. 添加为新节点
        User user = userApiRepository.findOne(uid);
        log.info("updateMember user: {}", JSON.toJSONString(user));
        String parentFCode = user.getParentFcode();

        short identity = IdentityConstant.NONE.getIdentity();

        log.info("countMemberuid={},parentFCode={}", uid, parentFCode);

        if (parentFCode == null || "".equals(parentFCode)) {
            // 顶级节点初始化
            pid = 0L;
            // 顶级用户身份为联合创始人
            identity = IdentityConstant.CO_FOUNDER.getIdentity();
        } else {
            User parent = userApiRepository.findUserByFcode(parentFCode);
            pid = parent.getUid();
        }
        log.info("countMemberidentityuid={},parentFCode={},identity={}", uid, parentFCode, identity);

        MemberTeam memberTeam = new MemberTeam(uid, pid, 0, identity,
                IdentityUpgradeMethod.AUTO.getMethod(), new Date(), user.getChannelId(), 0);
        log.info("memberTeam: {}", memberTeam);
        memberTeamRepository.saveAndFlush(memberTeam);
        return pid;
    }

    /**
     * 更新上级、上级身份和下级数量
     *
     * @param order
     */
    private void updateParentFcode(Order order) {
        User currentUser = order.getUser();
        // 关系绑定：上级为空并且邀请人为会员才能进行关系绑定。
        String invitationCode = order.getInvitationCode();
        if (StringUtils.isNotBlank(invitationCode)) {
            User invitationUser = userApiRepository.findUserByFcodeAndStatus(invitationCode, (short) 1);

            currentUser.setParentFcode(invitationCode);
            currentUser.setChannelId(invitationUser.getChannelId());
            // 设置所属路径
            currentUser.setPath(invitationUser.getPath() + "/" + currentUser.getUid());
            currentUser.setBindTime(new Date());
            currentUser = userApiRepository.saveAndFlush(currentUser);
            //这里重新setUser，否则下面的返利方法获取的上级可能是老的上级
            order.setUser(currentUser);

            // 统计下级数量
//            lowLevelCountService.addCountByBindingRelationship(invitationUser.getUid(), invitationUser.getParentFcode());
            // 清除缓存
            this.identityServer.removeAllSysKey(currentUser.getUid());
//            String key = RedisConstant.USER_SESSION_PREFIX + currentUser.getOpenId();
//            redisUtils.remove(key);

            updateParentIdentity(invitationUser.getUid(), currentUser.getPhone());
            log.info("updateParentFcode invitationCode: {}, currentUser: {}", invitationCode, JSON.toJSONString(currentUser));
            log.info("order currentUser: {}", JSON.toJSONString(order.getUser()));
            log.info("用户 [{}] 绑定上级 [{}]，并进行数据统计", currentUser.getUid(), invitationCode);
        } else {
            log.info("用户 [{}] 无需更新上级", currentUser.getUid());
        }
    }

    /**
     * 更新上级经理身份
     *
     * @param puid
     * @param phone
     */
    private void updateParentIdentity(Long puid, String phone) {
        MemberTeam memberTeamByUserId = this.memberTeamRepository.findMemberTeamByUserId(puid);
        log.info("updateParentIdentity,puid={},phone={},memberTeamByUserId={}", puid, phone, JSON.toJSONString(memberTeamByUserId));
        //判断邀请人是否是团购给其名额，如果已经消耗超过3个，需要更改身份为经理
        if (memberTeamByUserId != null && memberTeamByUserId.getIdentity() == IdentityConstant.NONE.getIdentity()) {

            PreBuyItem preBuyItem = preBuyItemRepository.findPreBuyItemByBindingPhoneAndUseState(phone, PreBuyItemUseState.UN_USE.getState());
            log.info("updateParentIdentity,preBuyItem={}", JSON.toJSONString(preBuyItem));
            if (preBuyItem != null) {
                PreBuy preBuy = preBuyItem.getPreBuy();
                log.info("updateParentIdentity,preBuy={}", JSON.toJSONString(preBuy));
                if (preBuy.getTransferorUid() != null && preBuy.getTransferorUid() > 0) {
                    //转让获取的团购，不算
                    return;
                }
                if (puid.equals(preBuy.getUserId())) {
                    Integer useNum = memberTeamByUserId.getUseNum() + 1;
                    if (useNum >= 3) {
                        //升级经理
                        memberTeamByUserId.setIdentity(IdentityConstant.MANAGER.getIdentity());
                        memberTeamByUserId.setUpgradeMethod((short) 2);
                        memberTeamByUserId.setUpgradeTime(new Date());
                    }
                    //更新团购已使用数量
                    memberTeamByUserId.setUseNum(useNum);
                    memberTeamRepository.save(memberTeamByUserId);
                    log.info("updateParentIdentity,useNum={}", useNum);
                }
            }
        }
    }

    /**
     * 监控订单状态，jdState分别是 16，19.分别对应 待收货、已完成
     *
     * @param uid 用户id
     */
    @Override
    public void monitorAndUpdateOrderState(String uid) {
        System.err.println("uid:" + uid);
        Object json = redisUtils.hmGet(RedisConstant.ORDER_IN_THE_ROUGH_HASH, uid);
        if (json == null) {
            return;
        }
        String jsonString = JSON.toJSONString(json);
        System.err.println(jsonString);
        if (Strings.isNullOrEmpty(jsonString)) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Set<String> jdOrderSet = jsonObject.keySet();
        int size = jdOrderSet.size();
        String[] jdOrderArray = new String[size];
        jdOrderArray = jdOrderSet.toArray(jdOrderArray);
        for (String jdOrder : jdOrderArray) {
            Long tradeNo = Long.valueOf(jdOrder);
            // state存储三个状态 0，16，19。其中 16，19为jd订单状态
            Integer state = jsonObject.getInteger(jdOrder);
            Integer jdOrderState;
            try {
                // 查询状态
                jdOrderState = judgeOrderState(tradeNo);
            } catch (Exception e) {
                log.error(e.getMessage());
                continue;
            }
            // 非监控状态，不做处理
//            if (jdOrderState != JDConstant.ORDER_STATE_WAITING_CONFIRM_RECEIVING_GOODS && jdOrderState != JDConstant.ORDER_STATE_DONE) {
//                continue;
//            }
            // 状态修改为待收货
            short updateState;
            boolean stateEffective = (state == 0 || state == JDConstant.ORDER_STATE_WAITING_CONFIRM_RECEIVING_GOODS);
            if ((jdOrderState == null) || !stateEffective) {
                continue;
            }
            if (jdOrderState == JDConstant.ORDER_STATE_WAITING_CONFIRM_RECEIVING_GOODS) {
                // 若状态未改变，执行下一个判断
                if (state.equals(jdOrderState)) {
                    continue;
                }
                // 更新订单状态
                jsonObject.put(jdOrder, jdOrderState);
                updateState = Constant.ORDER_STATUS_SEND_GOODS;
            } else if (jdOrderState == JDConstant.ORDER_STATE_CANCEL) {
                jsonObject.put(jdOrder, jdOrderState);
                updateState = Constant.ORDER_STATUS_CANCELED;
            } else {
                // 将状态修改为已完成，并将其从队列中删除
                jsonObject.remove(jdOrder);
                updateState = Constant.ORDER_STATUS_DONE;
            }

            try {
                int update = updateOrderStatusByJdOrder(updateState, tradeNo);
                if (update < 1) {
                    log.error("订单状态更新失败，jd订单号为 [{}], 须将状态更新为 [{}]", tradeNo, updateState);
                }
            } catch (Exception e) {
                log.error("订单状态更新失败，jd订单号为 [{}], 须将状态更新为 [{}]", tradeNo, updateState);
            }
        }
        // 若jsonObject为空了，将该键值从hash中删除
        if (jsonObject.size() < 1) {
            redisUtils.hDel(RedisConstant.ORDER_IN_THE_ROUGH_HASH, uid);
        } else {
            redisUtils.hmSet(RedisConstant.ORDER_IN_THE_ROUGH_HASH, uid, jsonObject);
        }
    }

    /**
     * 由于京东接口对于拆分订单的父订单的物流状态设置为【5：订单暂停】，所以需要根据子订单状态更新相应父订单状态。
     * 其中，子订单状态确定父订单状态的规则如下：
     * 已发货: 已发货子订单之一“已发货”状态
     * 已完成: 至少满足以下某个条件：
     * (1)所有子订单都是已完成状态；(2)所有子订单之中至少有1个是已完成状态，其他要么都是以完成或者已取消状态；
     * 已取消： 所有子订单都是已取消状态，或者父订单本身状态是已取消状态
     * 注：拆分订单之前的状态规则仍旧保持不变
     *
     * @param jdOrderId
     * @return
     */
    private Integer judgeOrderState(Long jdOrderId) {
        String url = URLConstant.JD_ORDER_DETAIL + "&tradeNo={tradeNo}";
        ResponseEntity<JDResult> entity = restTemplate.getForEntity(url, JDResult.class, jdOrderId);
        OrderDetailTo orderDetailTo = JDUtil.dealRequestData(entity, OrderDetailTo.class, false);
        if (orderDetailTo.getCOrder() == null || orderDetailTo.getCOrder().isEmpty()) {
            //没有拆单的状态查询后直接返回
            return orderDetailTo.getJdOrderState();
        }
        List<COrderDetailTo> cOrders = orderDetailTo.getCOrder();
        Map<Integer, Integer> jdOrderStatesMap = cOrders.stream().collect(Collectors.toMap(COrderDetailTo::getJdOrderState, COrderDetailTo::getOrderState));
        int deliveredCount = 0;
        int doneCount = 0;
        int cancelCount = 0;
        for (Integer jdOrderState : jdOrderStatesMap.keySet()) {
            if (jdOrderState == JDConstant.ORDER_STATE_WAITING_CONFIRM_RECEIVING_GOODS) {
                deliveredCount++;
                break;
            } else if (jdOrderState == JDConstant.ORDER_STATE_DONE) {
                doneCount++;
            } else if (jdOrderStatesMap.get(jdOrderState) == JDConstant.ORDER_STATE_CANCEL) {
                cancelCount++;
            }
        }
        if (deliveredCount > 0) {
            return JDConstant.ORDER_STATE_WAITING_CONFIRM_RECEIVING_GOODS;
        }
        if ((doneCount == cOrders.size()) || (doneCount + cancelCount == cOrders.size())) {
            return JDConstant.ORDER_STATE_DONE;
        }
        if ((cancelCount == cOrders.size()) || orderDetailTo.getOrderState() == JDConstant.ORDER_STATE_CANCEL) {
            return JDConstant.ORDER_STATE_CANCEL;
        }
        return null;
    }

    @Override
    public void executeRebate(OrderRebateBo orderRebateBo) {
        if (orderRebateBo == null) {
            throw new DataNotFoundException(ResultCode.ORDER_NOT_EXIST);
        }
        extraRebate(orderRebateBo);
    }

    /**
     * 根据订单项 id 更新并获取售后信息
     *
     * @param orderItemId 订单项id
     * @param jdOrderId   jd子订单号
     */
    @Override
    public List<AfterSale> updateAfterSaleStateByItemId(Long orderItemId, String jdOrderId) {
        OrderItem orderItem = orderItemRepository.findOne(orderItemId);
        if (orderItem == null) {
            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
        }
        Long skuId = orderItem.getSkuId();
        // 2. 查询售后表中的售后状态，若为已完成/已取消结束流程
        List<AfterSale> afterSaleList = afterSaleRepository.findByOrderItemIdAndState(orderItemId, (short) 1);
        List<AfterSale> resultList = new ArrayList<>();
//        if (afterSaleList == null || afterSaleList.size() < 1) {
//            throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
//        }
        if (CollectionUtils.isEmpty(afterSaleList)) {
            afterSaleList = new ArrayList<>(2);
            afterSaleList.add(null);
        }
        for (AfterSale afterSale : afterSaleList) {
            if (afterSale != null && afterSale.getStatus() >= AfterSaleStatus.DONE.getStatus()) {
                resultList.add(afterSale);
                continue;
            }
            // 3. 重新查询售后信息
            if (afterSale == null) {
                if (Strings.isNullOrEmpty(jdOrderId)) {
                    // 进行拆单查询获取子单号
                    Order order = orderItem.getOrder();
                    jdOrderId = dealDemolitionOrder(order.getJdOrderId().toString(), skuId, restTemplate);
                }
            } else {
                jdOrderId = afterSale.getJdOrderId().toString();
            }
            QueryAfterSaleInfoTo queryAfterSaleInfoTo = new QueryAfterSaleInfoTo();
            queryAfterSaleInfoTo.setJdOrderId(jdOrderId);
            HttpEntity<MultiValueMap> httpEntity;
            try {
                httpEntity = encapsulateFormData(queryAfterSaleInfoTo);
            } catch (IllegalAccessException e) {
                throw new BusinessException("数据封装出错");
            }
            ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_AFTER_SALE_INFO, httpEntity, JDResult.class);
            ParseAfterSaleInfo parseAfterSaleInfo = JDUtil.dealRequestData(entity, ParseAfterSaleInfo.class, true);
            List<AfterSaleInfoTo> list = parseAfterSaleInfo.getList();
            if (list == null) {
                log.info("暂无售后信息");
                return new ArrayList<>();
            }
            for (AfterSaleInfoTo afterSaleInfoTo : list) {
                if (skuId.equals(afterSaleInfoTo.getProductId())) {
                    afterSale = CommonUtil.dataHandler(afterSaleInfoTo, afterSale == null ? new AfterSale() : afterSale);
                    if (afterSale.getOrderItemId() == null) {
                        afterSale.setOrderItemId(orderItemId);
                    }
                    break;
                }
            }
            // 4. 更新售后库，并更新订单项售后状态
            if (afterSale == null) {
                log.info("暂无售后信息");
                return new ArrayList<>();
            }
            afterSale.setState(Constant.AFTER_SALE_STATE_IN_USE);
            afterSaleRepository.save(afterSale);
            resultList.add(afterSale);
            // 5. 若售后已经完成，则减少返利并生成退款账单
            boolean needRefund = StringUtils.isNotEmpty(afterSale.getRemarks()) && afterSale.getRemarks().contains("财务已退款");
            if (AfterSaleType.RETURN_GOODS.getType() == afterSale.getServiceType() && AfterSaleStatus.DONE.getStatus() == afterSale.getStatus() && needRefund) {
                // 减少退款返利
                deductionRefundRebate(orderItem);
                // 生成退款账单
                AfterSaleRefund afterSaleRefund = new AfterSaleRefund(afterSale.getId(), afterSale.getPrice().subtract(new BigDecimal(afterSale.getCount())), (short) 0);
                afterSaleRefundRepository.save(afterSaleRefund);
            }
        }
        return resultList;
    }

    @Override
    public AfterSaleAddressAndProblemDescTo getAfterSaleAddressAndDesc(QueryAddressAndDescVo addressAndDescVo) {
        Order destOrder = orderApiRepository.findOne(addressAndDescVo.getOrderId());
        if (destOrder == null) {
            throw new BusinessException(ResultCode.HQ_ORDER_NOT_EXIST);
        }
        List<Dictionary> dataList = dictionaryRepository.findByDictValueAndIsUsed(Constant.DICT_AFTER_SALE_PROBLEM_DESC, Constant.DICT_IN_USE);
        if (CollectionUtils.isEmpty(dataList)) {
            throw new BusinessException(ResultCode.DICT_DATA_NOT_FOUND);
        }
        AfterSaleAddressAndProblemDescTo result = new AfterSaleAddressAndProblemDescTo();
        if (destOrder.getProvinceId() != null) {
            BeanUtils.copyProperties(destOrder, result);
        }
        List<String> descs = dataList.stream().map(Dictionary::getDataValue).collect(Collectors.toList());
        result.setProblemDesc(descs);
        // 查询商品返回京东的方式
        String url = URLConstant.JD_AFTER_SALE_RETURN_TYPE + "&jdOrderId={jdOrderId}&skuId={skuId}";
        String jdOrderId = dealDemolitionOrder(addressAndDescVo.getJdOrderId().toString(), addressAndDescVo.getSkuId(), restTemplate);
        if (StringUtils.isEmpty(jdOrderId)) {
            // TODO 为空处理
//            return
        }
        ResponseEntity<JDResult> entity = restTemplate.getForEntity(url, JDResult.class, jdOrderId, addressAndDescVo.getSkuId());
        AfterSaleReturnTypeList returnTypeList = JDUtil.dealRequestData(entity, AfterSaleReturnTypeList.class, true);
        List<AfterSaleReturnType> returnTypes = returnTypeList.getList();
        // 包装返件方式描述
        if (!CollectionUtils.isEmpty(returnTypes)) {
            for (AfterSaleReturnType returnType : returnTypes) {
                returnType.setName(RETURN_TYPES.get(returnType.getCode()));
            }
            result.setReturnTypes(returnTypeList.getList());
        }

        return result;
    }

    @Override
    public void judgeProductAfterSaleStatus(List<OrderItemDto> orderItemDtos) {
        if (CollectionUtils.isEmpty(orderItemDtos)) {
            return;
        }
        List<Long> itemIds = orderItemDtos.stream().map(OrderItemDto::getId).collect(Collectors.toList());
        List<AfterSale> afterSaleOrders = afterSaleRepository.getRecentOrder(itemIds);
        if (CollectionUtils.isEmpty(afterSaleOrders)) {
            orderItemDtos.forEach(itemDto -> {
                itemDto.setAfterSaleCanApply(true);
            });
            return;
        }
        Map<Long, AfterSale> saleOrdersMap = afterSaleOrders.stream().collect(Collectors.toMap(AfterSale::getOrderItemId, Function.identity()));
        for (OrderItemDto orderItemDto : orderItemDtos) {
            AfterSale afterSale = saleOrdersMap.get(orderItemDto.getId());
            if (afterSale != null) {
                // 只可申请一次
                orderItemDto.setAfterSaleCanApply(false);
            } else {
                orderItemDto.setAfterSaleCanApply(true);
            }
        }
    }

    @Override
    public AfterSaleDetailDTO getAfterSaleDetail(Integer serviceNo) {
        QueryAfterSaleDetail param = new QueryAfterSaleDetail();
        param.setAppendInfoSteps(Collections.singletonList(AfterSaleDetailAppendInfoEnum.APPEND_TRACKINFO.getStatus()));
        param.setAfsServiceId(serviceNo.toString());
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_AFTER_SALE_DETAIL, param, JDResult.class);
        return JDUtil.dealRequestData(entity, AfterSaleDetailDTO.class, false);
    }


    /**
     * 创建订单默认参数
     */
    private Order createOrderDefaultParams(User currentUser, OrderInfoVo orderInfoVo) {
        Order order = new Order();

        // 将下单信息复制到订单中
        BeanUtils.copyProperties(orderInfoVo, order);
        order.setAddress(orderInfoVo.getSplicingAddress());

        // 设置订单默认项
        order.setUser(currentUser);

        order.setOrderTime(new Date());
        order.setOrderStatus(Constant.ORDER_STATUS_NOT_PAY);    // 订单状态：待付款
        order.setState((short) 0);          // 订单未删除
        return order;
    }

    /**
     * 判定邀请码正确性，并计算大礼包订单
     */
    private boolean countingGiftOrder(User currentUser, OrderInfoVo orderInfoVo, Order order) {
        if (orderInfoVo.getOrderNakedPrice().intValue() != 398) {
            throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
        }
        boolean changeBinding = false;
        boolean teamBuy = false;
        String invitationCode = orderInfoVo.getInvitationCode();
        if (Strings.isNullOrEmpty(invitationCode)) {
            throw new ParameterInvalidException(ResultCode.INVITATION_CODE_ERROR);
        }
        System.out.println("countingGiftOrderinvitationCode==============" + invitationCode);
        /**
         *  这个判断invitationCode.length() > 8的意义是，需求要新添加一个渠道A，这个渠道A下面没有一个会员。
         *  如何产生第一个联创会员？规则就是这个渠道A本身的代言人（就是渠道注册时留的手机号的用户）购买一个大礼包，称为这个渠道的联创会员。
         *  购买成功后这个联创会员再分享，下面的用户就称为这个联创的会员
         *  如新添加一个渠道到 t_channel_provider 表，他的openId为10f75dab48ae487893330d5be41dd8a3，
         *  给这个渠道生成的购买链接为 http://jdmarket.h5.dongdongyouhui.net/jdSubstation/member-product-list.html?fcode=10f75dab48ae487893330d5be41dd8a3。
         *  此时的fcode就不是8位的
         */
        if (invitationCode.length() > 8) {
            // 渠道邀请
            ChannelProvider channel = channelProviderRepository.findChannelProviderByOpenId(invitationCode);
            if (channel == null) {
                throw new ParameterInvalidException(ResultCode.INVITATION_CODE_ERROR);
            }
            // 手机号验证
            String channelPhone = Long.toString(channel.getPhone());
            if (!channelPhone.equals(currentUser.getPhone())) {
                throw new ParameterInvalidException(ResultCode.INVITATION_CODE_ERROR);
            }
            // 将默认渠道改变为邀请渠道
            currentUser.setChannelId(channel.getId());
            // 设置路径
            currentUser.setPath("/" + currentUser.getUid());
            currentUser = userApiRepository.save(currentUser);
            // 清除缓存
            cleanUserCache(currentUser);
            //联创的邀请码需要清空，否则支付回调会查询不到上级报错
            order.setInvitationCode(null);
        } else {
            // 会员邀请
            // 验证邀请码正确性
            changeBinding = checkInvitationCode(currentUser, invitationCode, Constant.ORDER_FROM_GIFT);
            // 判断购买用户是否是团购用户
            teamBuy = checkingTeamBuyUser(currentUser, changeBinding, invitationCode);
            if (changeBinding) {
                //绑定邀请码到订单表中，给支付回调做更新上下级和下级统计使用
                order.setInvitationCode(invitationCode);
            }
        }
        // 不能重复购买
        Long uid = currentUser.getUid();
        Member member = memberRepository.findMemberByUserId(uid);
        if (member != null) {
            throw new BusinessException(ResultCode.MEMBER_REPEAT_BUY);
        }
        // 大礼包只能单独下单，并且只能购买一个
        List<OrderItemVo> orderItemVos = orderInfoVo.getOrderItemVos();
        OrderItemVo orderItemVo = orderItemVos.get(0);
        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem orderItem = CommonUtil.dataHandler(orderItemVo, new OrderItem());
        orderItem.setOrder(order);
        BigDecimal salePrice = orderItem.getSalePrice();
        BigDecimal floorPrice = orderItem.getFloorPrice();
        BigDecimal orderNakedPrice = orderInfoVo.getOrderNakedPrice();
        if (salePrice.compareTo(orderNakedPrice) != 0) {
            throw new ParameterInvalidException(ResultCode.ORDER_AMOUNT_ERROR);
        }
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        // 订单裸价
        order.setOrderNakedPrice(orderInfoVo.getOrderNakedPrice());
        // 计算订单总价 = 订单裸价 + 运费
        order.setOrderPrice(salePrice);
        order.setOrderFloorPrice(floorPrice);
        order.setOrderFloorNakedPrice(floorPrice);

        if (teamBuy) {
            // 设置团购支付价格
            order.setPayPrice(new BigDecimal(Double.toString(Constant.TEAM_BUY_GIFT_PRICE)));
        } else {
            order.setPayPrice(salePrice);
        }
        return changeBinding;
    }


    /**
     * 校验传参的价格和商品库的价格是否一致
     *
     * @param currentUser
     * @param orderInfoVo
     */
    private void validPrice(User currentUser, OrderInfoVo orderInfoVo) {
        short characterType = currentUser.getCharacterType();
        StringBuffer skus = new StringBuffer();
        for (OrderItemVo orderItemVo : orderInfoVo.getOrderItemVos()) {
            skus.append(",").append(orderItemVo.getSkuId());
        }

        List<ProductDTO> data = this.productFacade.getProductListBySkus(skus.substring(1).toString());
        Map<Long, ProductDTO> map = new HashMap<>();
        for (ProductDTO dto : data) {
            map.put(dto.getSku(), dto);
        }
        for (OrderItemVo orderItemVo : orderInfoVo.getOrderItemVos()) {
            Long skuId = orderItemVo.getSkuId();
            ProductDTO productDTO = map.get(skuId);
            if (BigDecimal.valueOf(productDTO.getPurchasePrice()).compareTo(orderItemVo.getFloorPrice()) != 0) {
                throw new BusinessException(ResultCode.PRICE_CHANGED);
            }
            //京卡会员，或体验卡会员
            if (characterType > Constant.USER_NORMAL_TYPE || (currentUser.getExpCardType() != null && currentUser.getExpCardType() > 0)) {
                // 计算平台会员价格
                if (BigDecimal.valueOf(productDTO.getMemberPrice()).compareTo(orderItemVo.getSalePrice()) != 0) {
                    throw new BusinessException(ResultCode.PRICE_CHANGED);
                }
            } else {
                // 计算平台普通用户价格
                if (BigDecimal.valueOf(productDTO.getJdPrice()).compareTo(orderItemVo.getSalePrice()) != 0) {
                    throw new BusinessException(ResultCode.PRICE_CHANGED);
                }
            }
        }
    }

    /**
     * 计算jd订单
     */
    private boolean countingJDOrder(User currentUser, OrderInfoVo orderInfoVo, Order order) {
        // 验证邀请码
        boolean changeBinding = checkInvitationCode(currentUser, orderInfoVo.getInvitationCode(), Constant.ORDER_FROM_JD);

        // 计算订单总价格
        short characterType = currentUser.getCharacterType();
        BigDecimal nakedOrderPrice = new BigDecimal(0);
        BigDecimal nakedOrderFloorPrice = new BigDecimal(0);
        List<OrderItem> orderItemList = new ArrayList<>();
        List<Long> skuIds = new ArrayList<>();
        for (OrderItemVo orderItemVo : orderInfoVo.getOrderItemVos()) {
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(orderItemVo, orderItem);
            skuIds.add(orderItem.getSkuId());
            orderItem.setOrder(order);
            orderItemList.add(orderItem);
            BigDecimal itemNum = new BigDecimal(orderItem.getNum());
            // 计算底价价格
            BigDecimal floorPrice = orderItem.getFloorPrice();
            nakedOrderFloorPrice = nakedOrderFloorPrice.add(floorPrice.multiply(itemNum));

            //京卡会员或体验卡会员
            if (characterType > Constant.USER_NORMAL_TYPE || (currentUser.getExpCardType() != null && currentUser.getExpCardType() > 0)) {
                // 计算平台会员价格
                BigDecimal memberPrice = orderItem.getMemberPrice();
                memberPrice = memberPrice.multiply(itemNum);
                nakedOrderPrice = nakedOrderPrice.add(memberPrice);
            } else {
                // 计算平台普通用户价格
                BigDecimal price = orderItem.getPlatformPrice();
                price = price.multiply(itemNum);
                nakedOrderPrice = nakedOrderPrice.add(price);
            }
        }

        // 比较订单裸价（在不含运费的情况下），是否相等
        if (nakedOrderPrice.compareTo(orderInfoVo.getOrderNakedPrice()) != 0) {
            throw new BusinessException(ResultCode.ORDER_AMOUNT_ERROR);
        }
        // 运费
        BigDecimal freight = new BigDecimal(order.getFreight());
        // 订单裸价
        order.setOrderNakedPrice(orderInfoVo.getOrderNakedPrice());
        // 计算订单总价 = 订单裸价 + 运费
        order.setOrderPrice(nakedOrderPrice.add(freight));
        order.setOrderItems(orderItemList);

        // 底价裸价
        order.setOrderFloorNakedPrice(nakedOrderFloorPrice);
        // 底价总价
        order.setOrderFloorPrice(nakedOrderFloorPrice.add(freight));
        // 设置支付价格
        order.setPayPrice(order.getOrderPrice());

        // 检查产品是否属于购物车并删除
        deleteFromShoppingCart(currentUser, skuIds);
        return changeBinding;
    }

    /**
     * 校验邀请码正确性
     */
    private boolean checkInvitationCode(User currentUser, String invitationCode, short orderFrom) {
        // 无上级，并且不存在邀请码，无法购买
        if (Strings.isNullOrEmpty(invitationCode)) {
            return invitationCodeRes(orderFrom);
        }
        // 验证邀请码正确性
        User parentUser = userApiRepository.findUserByFcodeAndStatus(invitationCode, (short) 1);
        if (parentUser == null) {
            return invitationCodeRes(orderFrom);
        }
        // 验证邀请用户是否会员
        Member parentMember = memberRepository.findMemberByUserId(parentUser.getUid());
        if (parentMember == null) {
            return invitationCodeRes(orderFrom);
        }
        return true;
    }

    /**
     * 根据订单类型执行不同的下单效果
     * 大礼包：抛出邀请码异常
     * 普通商品：不进行上下级绑定
     *
     * @param orderFrom
     * @return
     */
    private boolean invitationCodeRes(short orderFrom) {
        if (orderFrom == Constant.ORDER_FROM_GIFT) {
            throw new ParameterInvalidException(ResultCode.INVITATION_CODE_ERROR);
        } else {
            return false;
        }
    }

    /**
     * 检查是否为团购用户
     */
    private boolean checkingTeamBuyUser(User currentUser, boolean changeBinding, String invitationCode) {
        //直接用最新的邀请人查询
        User parentUser = userApiRepository.findUserByFcode(invitationCode);
        String phone = currentUser.getPhone();
        // 检查团购包中是否存在该绑定用户
        PreBuyItem preBuyItem = preBuyItemRepository.findPreBuyItemByBindingPhoneAndUseState(phone, PreBuyItemUseState.UN_USE.getState());
        if (preBuyItem == null) {
            return false;
        }
        // 检查存在的团购包是否属于其对应上级
        PreBuy preBuy = preBuyItem.getPreBuy();
        if (!parentUser.getUid().equals(preBuy.getUserId())) {
            //需要作废之前别人给他的那条团购记录
            InvalidParam param = new InvalidParam();
            param.setPhone(phone);
            param.setPreBuyItemId(preBuyItem.getId());
            this.preBuyService.invalid(param);
            return false;
        }
        return true;
    }

    /**
     * 团购返利，返利完成后再修改身份
     *
     * @param orderRebateBo
     */
    private void extraRebate(OrderRebateBo orderRebateBo) {
        ExtraRebateTo extraRebateTo = rebate(orderRebateBo);
        log.info("extraRebate,uid={},identity={}", extraRebateTo.getUid(), orderRebateBo.getIdentity());
        if (extraRebateTo == null) {
            return;
        }
        new Thread(() -> {
            boolean res = applyExtraRebate(extraRebateTo);
            //2019-9-17 新需求，必须消耗3个团购名额才升级为经理
//            if (res) {
//                MemberTeam memberTeam = memberTeamRepository.findMemberTeamByUserId(extraRebateTo.getUid());
//                short identity = orderRebateBo.getIdentity();
//                if (identity > memberTeam.getIdentity()) {
//                    memberTeam.setIdentity(identity);
//                    memberTeam.setUpgradeMethod((short) 2);
//                    memberTeam.setUpgradeTime(new Date());
//                    memberTeamRepository.saveAndFlush(memberTeam);
//                }
//            }
        }).start();
    }

    /**
     * 普通购买时修改身份
     *
     * @param orderRebateBo
     */
    private void normalRebate(OrderRebateBo orderRebateBo) {
        ExtraRebateTo extraRebateTo = rebate(orderRebateBo);
        if (extraRebateTo == null) {
            return;
        }
        new Thread(() -> applyExtraRebate(extraRebateTo)).start();
    }

    /**
     * 返利操作
     *
     * @param orderRebateBo 订单业务操作对象
     */
    private ExtraRebateTo rebate(OrderRebateBo orderRebateBo) {
        // 计算返利
        int totalRebateFee = orderRebate(orderRebateBo);
        Long orderId = orderRebateBo.getId();
        UserRebateBo userRebateBo = orderRebateBo.getUserRebateBo();
        Long uid = userRebateBo.getUid();
        User user = userApiRepository.findOne(uid);
        if (totalRebateFee < 1) {
            // 总奖励数小于1，不进行返利
            log.info("总返利数太小，不能进行返利，consumerUid = [{}]，orderId = [{}]，totalRebateFee = [{}]", uid, orderId, totalRebateFee);
            return null;
        }
        log.debug("总返利数值信息，consumerUid = [{}]，orderId = [{}]，totalRebateFee = [{}]", uid, orderId, totalRebateFee);
        Short orderFrom = orderRebateBo.getOrderFrom();
        Short orderType = orderRebateBo.getOrderType();
        // 查询返利比例问题
        Rate rate = rateApiRepository.findRateByChannelIdAndRebateType(userRebateBo.getChannelId(), orderFrom);
        if (rate == null) {
            log.error("用户返利率查询失败，consumerUid = [{}]，orderId = [{}]", uid, orderId);
            return null;
        }
        BigDecimal channelRate = new BigDecimal(Float.toString(rate.getChannelRate()));
        int channelRebateFee = channelRate.multiply(new BigDecimal(totalRebateFee)).intValue();
        if (channelRebateFee < 1) {
            // 渠道奖励数小于1，不进行返利
            log.info("渠道返利数太小，不能进行返利，consumerUid = [{}]，orderId = [{}]，channelRebateFee = [{}]", uid, orderId, channelRebateFee);
            return null;
        }
        log.debug("渠道总返利数值信息，consumerUid = [{}]，orderId = [{}]，channelRebateFee = [{}]", uid, orderId, channelRebateFee);
        BigDecimal orderNakedPrice = orderRebateBo.getOrderNakedPrice();
        // 利润计算

        BigDecimal firstRate = new BigDecimal(Float.toString(rate.getFirstRate()));
        BigDecimal secondRate = new BigDecimal(Float.toString(rate.getSecondRate()));
        String phone = userRebateBo.getPhone();
        Date now = new Date();
        // 入账时间，大礼包为立即入账，jd 产品入账时间为一月之后
        Date accountEntryTime = CommonUtil.getAfterDate(Calendar.MONTH, 1, now);
        Short accountEntryType = Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY;
        // 返利类型
        short firstLevel = Constant.REBATE_TYPE_JD_FIRST_LEVEL;
        short secondLevel = Constant.REBATE_TYPE_JD_SECOND_LEVEL;
        short channelLevel = Constant.REBATE_TYPE_JD_CHANNEL;
        // 根据订单类型决定返利时期
        if (orderFrom == Constant.ORDER_FROM_GIFT) {
            // 大礼包，立即返利
            accountEntryTime = now;
            accountEntryType = Constant.REBATE_STATE_UNCHECKED;
            firstLevel = Constant.REBATE_TYPE_GIFT_FIRST_LEVEL;
            secondLevel = Constant.REBATE_TYPE_GIFT_SECOND_LEVEL;
            channelLevel = Constant.REBATE_TYPE_GIFT_CHANNEL;
        }

        String firstFCode;
        if (orderType == Constant.ORDER_TYPE_PRE_BUY) {
            firstFCode = user.getFcode();
        } else {
            firstFCode = userRebateBo.getParentFcode();
        }
        // 无上级，所有利润返给渠道上
        if (!Strings.isNullOrEmpty(firstFCode)) {
            int res;
            // 一级返利
            User firstParentUser;
            UserRebateBo firstUserRebateBo = null;
            if (orderType == Constant.ORDER_TYPE_PRE_BUY) {
                firstParentUser = user;
            } else {
                firstParentUser = userApiRepository.findUserByFcodeAndStatus(firstFCode, (short) 1);
            }
            if (firstParentUser != null) {
                firstUserRebateBo = CommonUtil.dataHandler(firstParentUser, new UserRebateBo());
            }
            // 判断当前上级是否是会员
            res = adjustFirstOrSecondRebate(orderFrom, orderType, totalRebateFee, orderId, orderNakedPrice, uid, firstRate,
                    phone, now, accountEntryTime, firstUserRebateBo, accountEntryType, firstLevel);
            if (res > 0) {
                channelRebateFee -= res;
                log.debug("一级返利数值信息，consumerUid = [{}]，orderId = [{}]，firstRebateFee = [{}]", uid, orderId, res);
            }
            // 二级返利
            if (firstParentUser != null) {
                String secondFCode = firstParentUser.getParentFcode();
                if (!Strings.isNullOrEmpty(secondFCode)) {
                    User secondParentUser;
                    UserRebateBo secondUserRebateBo = null;
                    if (orderType == Constant.ORDER_TYPE_PRE_BUY) {
                        secondParentUser = Strings.isNullOrEmpty(user.getParentFcode()) ? null : userApiRepository.findUserByFcodeAndStatus(user.getParentFcode(), (short) 1);
                    } else {
                        secondParentUser = userApiRepository.findUserByFcodeAndStatus(secondFCode, (short) 1);
                    }
                    if (secondParentUser != null) {
                        secondUserRebateBo = CommonUtil.dataHandler(secondParentUser, new UserRebateBo());
                    }
                    // 判断当前上级是否是会员
                    res = adjustFirstOrSecondRebate(orderFrom, orderType, totalRebateFee, orderId, orderNakedPrice, uid, secondRate,
                            phone, now, accountEntryTime, secondUserRebateBo, accountEntryType, secondLevel);
                    if (res > 0) {
                        channelRebateFee -= res;
                        log.debug("二级返利数值信息，consumerUid = [{}]，orderId = [{}]，secondRebateFee = [{}]", uid, orderId, res);
                    }
                }
            }
        }
        // 渠道返利
        Long channelId = userRebateBo.getChannelId();
        ChannelProvider channel = channelProviderRepository.findOne(channelId);
        if (channel == null) {
            log.error("用户渠道不存在，uid = [{}]，channelId = [{}]", uid, channelId);
            return null;
        }
        // 根据返利数，计算返利金额
        BigDecimal totalRebatePrice = new BigDecimal(totalRebateFee).movePointLeft(2);
        BigDecimal channelRebatePrice = new BigDecimal(channelRebateFee).movePointLeft(2);
        log.debug("渠道最终返利数值信息，consumerUid = [{}]，orderId = [{}]，channelRebateFee = [{}]", uid, orderId, channelRebateFee);
        Rebate channelRebate = new Rebate(orderId, uid, channelId, channelRebatePrice, accountEntryType,
                now, phone, orderType, orderNakedPrice, accountEntryTime, Constant.USER_REBATE_TYPE_CHANNEL, channelRebateFee, channelLevel, totalRebatePrice, channel.getPhone().toString());
        rebateApiRepository.saveAndFlush(channelRebate);
        // 修改总返利数
        updateTotalRebate(channelId, channelRebateFee, now, accountEntryType, Constant.USER_REBATE_TYPE_CHANNEL, channel.getPhone().toString());
        //  渠道奖励短信推送
//        sendMsg(channelRebatePrice, orderType, uid, (short) 0, orderFrom, userRebateBo, orderId);

        // 判断渠道是否支持额外返利
        if (channel.getRebateSupport() == Constant.REBATE_SUPPORT_EXTRA) {
            return new ExtraRebateTo(orderId, orderType, orderFrom, uid);
        }
        return null;
    }

    private boolean applyExtraRebate(ExtraRebateTo extraRebateTo) {
        //日志记录，防止调用失败，可以补偿操作
        RebateExp exp = new RebateExp(extraRebateTo.getOrderId(), extraRebateTo.getOrderType(), extraRebateTo.getRebateType(), extraRebateTo.getUid(), (short) 0);
        rebateExpRepository.save(exp);

        ResponseEntity<JsonResult> entity = restTemplate.postForEntity(extraRebateUrl, extraRebateTo, JsonResult.class);
        int statusCodeValue = entity.getStatusCodeValue();
        if (statusCodeValue == 200) {
            log.info("额外返利请求成功，用户id为 [{}]，订单id为 [{}]", extraRebateTo.getUid(), extraRebateTo.getOrderId());
            return true;
        } else {
            log.error("额外返利请求失败，用户id为 [{}]，订单id为 [{}]", extraRebateTo.getUid(), extraRebateTo.getOrderId());
        }
        return false;
    }

    /**
     * 判断是否进行一二级返利
     */
    private int adjustFirstOrSecondRebate(short orderFrom, short orderType, int rebateFee, Long
            orderId, BigDecimal orderNakedPrice, Long uid,
                                          BigDecimal adjustRate, String phone, Date now, Date aMouthLaterDate,
                                          UserRebateBo userRebateBo, short accountEntryType, short rebateType) {
        // 判断返利上级是否存在
        if (userRebateBo == null) {
            return 0;
        }
        // 判断返利上级用户曾是否入驻过会员
        Member member = memberRepository.findMemberByUserId(userRebateBo.getUid());
        if (member == null) {
            return 0;
        }
        // 开始返利
        BigDecimal totalRebateCount = new BigDecimal(rebateFee);
        BigDecimal totalRebatePrice = totalRebateCount.movePointLeft(2);
        BigDecimal rebatePrice = adjustRate.multiply(totalRebatePrice);
        // 大礼包返利四舍五入
        if (orderFrom == Constant.ORDER_FROM_GIFT) {
            rebatePrice = roundGiftRebate(rebatePrice.setScale(0, BigDecimal.ROUND_HALF_UP));
        }
        int rebateNum = rebatePrice.movePointRight(2).intValue();
        if (rebateNum < 1) {
            return 0;
        }
        // 解决大于两位小数的情况下，存入数据库自动四舍五入的情况
        rebatePrice = new BigDecimal(rebateNum).movePointLeft(2);
        Long adjustUserId = userRebateBo.getUid();
        Rebate rebate = new Rebate(orderId, uid, adjustUserId, rebatePrice, accountEntryType,
                now, phone, orderType, orderNakedPrice, aMouthLaterDate, Constant.USER_REBATE_TYPE_MEMBER, rebateNum, rebateType, totalRebatePrice, userRebateBo.getPhone());
        rebateApiRepository.saveAndFlush(rebate);
        updateTotalRebate(adjustUserId, rebateNum, now, accountEntryType, Constant.USER_REBATE_TYPE_MEMBER, userRebateBo.getPhone());
//         奖励信息推送
        sendMsg(rebatePrice, orderType, uid, rebateType, orderFrom, userRebateBo, orderId);
        return rebateNum;
    }

    /**
     * 增加返利时，修改总返利表
     *
     * @param uid              返利用户id
     * @param rebateNum        返利数
     * @param lastTime         上次返利时间
     * @param accountEntryType 是否入账，1 为未入账（代表正常购买的商品），2 为未审核（代表已入账，大礼包产品）
     * @param rebateUserType   返利用户类型，1 为 会员/分销，2 为渠道
     */
    private void updateTotalRebate(Long uid, Integer rebateNum, Date lastTime, short accountEntryType,
                                   short rebateUserType, String rewardUserPhone) {
        // 在总返利上增加返利数目
        // FIXME 加上修改版本， 乐观锁
        TotalRebate totalRebate = totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(uid, rebateUserType);
        if (totalRebate == null) {
            totalRebate = new TotalRebate(uid, 0, 0, 0, 0,
                    lastTime, rebateUserType, rewardUserPhone);
        }
        // 根据入账类型，操作总返利数
        if (accountEntryType == Constant.REBATE_STATE_NOT_ACCOUNT_ENTRY) {
            totalRebate.setUnAccountEntryTotalNum(totalRebate.getUnAccountEntryTotalNum() + rebateNum);
        } else {
            totalRebate.setAccountEntryTotalNum(totalRebate.getAccountEntryTotalNum() + rebateNum);
        }
        totalRebateApiRepository.saveAndFlush(totalRebate);
    }

    /**
     * 计算订单返利
     */
    private int orderRebate(OrderRebateBo orderRebateBo) {
//        taxationIncr: 增值税	0.000	1	1
//        taxationCash: 提现费	0.000	1	2
        Taxation taxationIncr = taxationApiRepository.findTaxationByTaxationTypeAndOrderType(Constant.TAXATION_TYPE_INCR, orderRebateBo.getOrderFrom());
        Taxation taxationCash = taxationApiRepository.findTaxationByTaxationTypeAndOrderType(Constant.TAXATION_TYPE_CASH, orderRebateBo.getOrderFrom());
        BigDecimal incrRate = taxationIncr.getTaxationRate();
        BigDecimal cashRate = taxationCash.getTaxationRate();
        BigDecimal zero = new BigDecimal(0);
        BigDecimal totalRebate = zero;
        List<OrderRebateItemBo> orderItems = orderRebateBo.getOrderItems();
        for (OrderRebateItemBo orderItem : orderItems) {
            totalRebate = totalRebate.add(countingOrderItemProfit(CommonUtil.dataHandler(orderItem, new OrderRebateItemBo()), incrRate, cashRate));//11.676
        }
        if (totalRebate.compareTo(zero) < 1) {
            log.error("订单利润小于税率，orderId = [{}]", orderRebateBo.getId());
        }
        return totalRebate.intValue();
    }

    /**
     * 计算订单项利润
     *
     * @param orderItem 订单项
     * @param incrRate  增值税率
     * @param cashRate  提现税率
     */
    private BigDecimal countingOrderItemProfit(OrderRebateItemBo orderItem, BigDecimal incrRate, BigDecimal
            cashRate) {
        BigDecimal salePrice = orderItem.getSalePrice().movePointRight(2);
        BigDecimal floorPrice = orderItem.getFloorPrice().movePointRight(2);
        if (salePrice.compareTo(floorPrice) < 1) {
            log.error("订单售价低于进价，orderItemId = [{}]，skuId = [{}]", orderItem.getId(), orderItem.getSkuId());
            return BigDecimal.ZERO;
        }
        // 先计算所有卖出个数总价，再计算返利。减少误差
        Integer num = orderItem.getNum();
        BigDecimal saleNum = new BigDecimal(Integer.toString(num));
        floorPrice = floorPrice.multiply(saleNum);// 125.8
        salePrice = salePrice.multiply(saleNum);//139.8
        // 计算返利
        BigDecimal subtract = salePrice.subtract(floorPrice);//14
        BigDecimal incrRateFee = subtract.multiply(incrRate);//2.24
        BigDecimal cashRateFee = salePrice.multiply(cashRate);//0.838
        BigDecimal rateFee = incrRateFee.add(cashRateFee);//2.324
        if (subtract.compareTo(rateFee) < 1) {
            log.error("订单利润小于税率，orderItemId = [{}]，skuId = [{}]", orderItem.getId(), orderItem.getSkuId());
            return BigDecimal.ZERO;
        }
        return subtract.subtract(rateFee);
    }

    /**
     * 将未支付订单放入延时处理器中
     *
     * @param key     订单号
     * @param orderId 订单id
     */
    private void putInOrderTimer(String key, Long orderId) {
        String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + key;
        long expireTime = Constant.ORDER_TIMER_EFFECTIVE + 1;
        redisUtils.set(orderNotPayKey, orderId, expireTime, TimeUnit.MINUTES);
        orderTimerService.put(orderNotPayKey);
    }

    /**
     * @param orderInfoVo 订单信息
     */
    private void dealOrderFromJD(OrderInfoVo orderInfoVo, Order order) {

        if (IS_TEST) {
            order.setHqOrderNum("test-hq-order-num");
            order.setJdOrderId(134321341L);
            return;
        }
        CreateOrderTo createOrderTo = new CreateOrderTo();
        BeanUtils.copyProperties(orderInfoVo, createOrderTo);
        List<OrderItemVo> orderItemVos = orderInfoVo.getOrderItemVos();
        Map<String, Integer> map = new HashMap<>();
        for (OrderItemVo orderItemVo : orderItemVos) {
            map.put(Long.toString(orderItemVo.getSkuId()), orderItemVo.getNum());
        }
        createOrderTo.setProducts(map);
        ResponseEntity<JDResult> entity = restTemplate.postForEntity(URLConstant.JD_CREATE_ORDER, createOrderTo, JDResult.class);
        OrderResultTo orderResultTo = JDUtil.dealRequestData(entity, OrderResultTo.class, false);

        // 判断订单价格是否正确
        BigDecimal hqNakedPrice = new BigDecimal(Double.toString(orderResultTo.getOrderPrice()));
        BigDecimal hqTotalPrice = new BigDecimal(Double.toString(orderResultTo.getTotalFee()));
        int compareVal = order.getOrderFloorNakedPrice().compareTo(hqNakedPrice);
        if (compareVal != 0
                || order.getFreight().compareTo(orderResultTo.getFeight()) != 0
                || order.getOrderFloorPrice().compareTo(hqTotalPrice) != 0) {
            try {
                log.info("dealOrderFromJDerrorjd={}", JSON.toJSONString(orderResultTo));
                log.info("dealOrderFromJDerrormy={}", JSON.toJSONString(order));
                // 请求商品系统触发商品价格实时更新
                List<Long> skus = order.getOrderItems().stream().map(OrderItem::getSkuId).collect(Collectors.toList());
                log.info("dealOrderFromJD updateDbProdcut,skus={}", JSON.toJSONString(skus));
                productFacade.updateProductRealTime(skus);
            } catch (Exception e) {
                log.error("dealOrderFromJD request product update error: {}", e);
            }
            throw new BusinessException(ResultCode.PRICE_CHANGED);
        }

        order.setHqOrderNum(orderResultTo.getOrderSn());
        order.setJdOrderId(Long.parseLong(orderResultTo.getJdTradeNo()));
    }

    /**
     * 处理sonnhe订单，大礼包等
     */
    private void dealOrderFromSonnhe(Order order) {
//        Order order = new Order();
        // 设置自营订单 hq 及 jd 的订单号
        order.setHqOrderNum("sonnhe");
        order.setJdOrderId(0L);
        // 设置自营订单 hq 价格
//        order.setHqOrderNakedPrice(new BigDecimal(0));
//        order.setHqOrderPrice(new BigDecimal(0));
    }

    /**
     * 使用团购绑定
     */
    private boolean consumeTeamBuy(User currentUser) {
        String phone = currentUser.getPhone();
        PreBuyItem preBuyItem = preBuyItemRepository.findPreBuyItemByBindingPhoneAndUseState(phone, PreBuyItemUseState.UN_USE.getState());
        if (preBuyItem == null) {
            return false;
        }
        preBuyItem.setUseState(PreBuyItemUseState.USED.getState());
        preBuyItem.setUsedTime(new Date());
        preBuyItemRepository.save(preBuyItem);
        return true;
    }

    /**
     * 会员产品，升级会员
     * FIXME 目前只能购买一次会员，并且会员有效期默认为一年
     */
    private void upgradeMember(Order order) {
        User user = order.getUser();
        log.info("upgradeMember order user: {}", JSON.toJSONString(user));
        Long uid = user.getUid();
        // 校验用户是否重复下单大礼包购买
        checkingRepeatBuyGift(uid);
        Date now = new Date();
        // 一年后
        Date afterDate = CommonUtil.getAfterDate(Calendar.YEAR, 1, now);
        Member member = new Member(uid, (short) 1, (short) 1, now, afterDate, (short) 1);
        memberRepository.saveAndFlush(member);
        if (Strings.isNullOrEmpty(user.getParentFcode())) {
            // 该用户为渠道的代理用户
            user.setCharacterType(Constant.USER_CHANNEL_PROVIDER_TYPE);
        } else {
            user.setCharacterType(Constant.USER_MEMBER_TYPE);
        }
        user = userApiRepository.saveAndFlush(user);
        //每次更新需要重新setUser
        order.setUser(user);

        cleanUserCache(user);
        log.info("会员升级成功，用户id为 [{}]", uid);
        log.info("upgradeMember save user: {}", JSON.toJSONString(user));

        // 下述操作为会员续期操作。暂不使用该方式

//        Integer num = memberOrderItem.getNum();   // 产品数量，每个产品代表一个月
//        User currentUser = identityServer.getCurrentUser();
//        Short characterType = currentUser.getCharacterType();
//        Long uid = currentUser.getUid();
//        Member member = memberRepository.findMemberByUserId(uid);
//        Distributor distributor = distributorRepository.findDistributorByUserId(uid);
//        Date now = new Date();
//        if (characterType == Constant.USER_NORMAL_TYPE) {
//            Date afterDate = CommonUtil.createAfterDate(num, now);
//            // 查看是否存在已过期会员信息
//            if (member == null) {
//                // 添加会员
//                member = new Member(uid, (short) 1, (short) 1, now, afterDate, (short) 1);
//            } else {
//                member.setState((short) 1);
//                member.setUpgradeTime(now);
//                member.setValidTime(afterDate);
//            }
//            memberRepository.save(member);
//            if (distributor == null) {
//                currentUser.setCharacterType(Constant.USER_MEMBER_TYPE);
//            } else {
//                currentUser.setCharacterType(Constant.USER_DISTRIBUTOR_TYPE);
//                distributor.setState((short) 1);
//                distributorRepository.save(distributor);
//            }
//            userApiRepository.save(currentUser);
//            redisUtils.remove(RedisConstant.USER_SESSION_PREFIX + currentUser.getOpenId());
//        } else if (characterType == Constant.USER_MEMBER_TYPE || characterType == Constant.USER_DISTRIBUTOR_TYPE) {
//            // 查询会员到期时间
//            if (member == null) {
//                throw new RuntimeException("会员不存在");
//            }
//            Date validTime = member.getValidTime();
//            validTime = CommonUtil.createAfterDate(num, validTime);
//            member.setValidTime(validTime);
//            memberRepository.save(member);
//        }
    }

    /**
     * 根据jd订单号更新订单状态
     *
     * @param jdOrder     jd订单号
     * @param orderStatus 订单状态
     */
    private int updateOrderStatusByJdOrder(short orderStatus, Long jdOrder) {
        return orderApiRepository.updateOrderStatusByJdOrder(jdOrder, orderStatus);
    }

    /**
     * 清除缓存
     *
     * @param user
     * @return
     */
    private User cleanUserCache(User user) {
        User updateUser = CommonUtil.dataHandler(user, new User());
//        // 更新缓存
//        String openId = updateUser.getOpenId();
//        String key = RedisConstant.USER_SESSION_PREFIX + openId;
//        redisUtils.hmSet(key, RedisConstant.SESSION_KEY_OPENID, openId);
//        redisUtils.hmSet(key, RedisConstant.SESSION_KEY_USER, JSON.toJSONString(updateUser));
//        redisUtils.expire(key, Long.parseLong(RedisConstant.OPENID_EFFECTIVE_TIME), TimeUnit.MINUTES);

        this.identityServer.removeAllSysKey(updateUser.getUid());
        return updateUser;
    }

    /**
     * 从购物车中删除
     */
    private void deleteFromShoppingCart(User currentUser, List<Long> skuIds) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(currentUser.getUid());
        for (Long skuId : skuIds) {
            int delete = shoppingCartItemRepository.deleteByShoppingCartAndSkuId(cart, skuId);
            if (delete == 1) {
                log.info("用户 [{}] 的产品 [{}] 从购物车中删除", currentUser.getPhone(), skuId);
            }
        }
    }

    /**
     * 减少退款返利
     */
    private void deductionRefundRebate(OrderItem orderItem) {
        // 返利减少
        Taxation taxationIncr = taxationApiRepository.findTaxationByTaxationTypeAndOrderType(Constant.TAXATION_TYPE_INCR, Constant.ORDER_FROM_JD);
        Taxation taxationCash = taxationApiRepository.findTaxationByTaxationTypeAndOrderType(Constant.TAXATION_TYPE_CASH, Constant.ORDER_FROM_JD);
        BigDecimal incrRate = taxationIncr.getTaxationRate();
        BigDecimal cashRate = taxationCash.getTaxationRate();
        // 售后相同产品的不同个数同样会分为不同服务单处理
        orderItem.setNum(1);
        BigDecimal profit = countingOrderItemProfit(CommonUtil.dataHandler(orderItem, new OrderRebateItemBo()), incrRate, cashRate);
        if (profit.compareTo(BigDecimal.ZERO) > 0) {
            Order order = orderItem.getOrder();
            List<Rebate> rebateList = rebateApiRepository.findByOrderId(order.getId());
            if (rebateList != null && rebateList.size() > 1) {
                // 判断包含的返利类型
                Map<Short, Rebate> mapping = new HashMap<>();
                rebateList.forEach(r -> mapping.put(r.getRebateType(), r));
                Rate rate = rateApiRepository.findRateByChannelIdAndRebateType(order.getUser().getChannelId(), Constant.ORDER_FROM_JD);
                BigDecimal channelRate = new BigDecimal(rate.getChannelRate().toString());
                BigDecimal firstRate = new BigDecimal(rate.getFirstRate().toString());
                BigDecimal secondRate = new BigDecimal(rate.getSecondRate().toString());
                BigDecimal surplus = BigDecimal.ZERO;
                Set<Short> types = mapping.keySet();
                Map<Integer, Rebate> profitMapping = new HashMap<>();
                Rebate channelRebate = null;
                int max = 0;
                for (Short type : types) {
                    switch (type) {
                        case Constant.REBATE_TYPE_JD_FIRST_LEVEL:
                            surplus = surplus.add(saveDeductionRefundRebate(profit, firstRate, mapping.get(type)));
                            break;
                        case Constant.REBATE_TYPE_JD_SECOND_LEVEL:
                            surplus = surplus.add(saveDeductionRefundRebate(profit, secondRate, mapping.get(type)));
                            break;
                        case Constant.REBATE_TYPE_JD_EXTRA:
                            Rebate r = mapping.get(type);
                            Integer rebateNum = r.getRebateNum();
                            if (max < rebateNum) {
                                max = rebateNum;
                            }
                            profitMapping.put(rebateNum, r);
                            break;
                        case Constant.REBATE_TYPE_JD_CHANNEL:
                            channelRebate = mapping.get(type);
                            break;
                        default:
                            break;
                    }
                }
                // 计算额外返利
                if (profitMapping.size() > 0) {
                    Set<Integer> set = profitMapping.keySet();
                    for (Integer num : set) {
                        if (max == num) {
                            surplus = surplus.add(saveDeductionRefundRebate(profit, Constant.MANAGER_RATE, profitMapping.get(num)));
                        } else {
                            surplus = surplus.add(saveDeductionRefundRebate(profit, Constant.CHIEF_RATE, profitMapping.get(num)));
                        }
                    }
                }
                if (channelRebate == null) {
                    throw new DataNotFoundException(ResultCode.DATA_NOT_EXIST_ERROR);
                }
                // 计算渠道返利
                saveDeductionRefundRebate(profit, channelRate.subtract(surplus), channelRebate);
            }
        }
    }

    /**
     * 保存退款返利记录
     *
     * @param profit 利润
     * @param rate   返利比例
     * @param rebate 返利记录
     * @return 返利比例
     */
    private BigDecimal saveDeductionRefundRebate(BigDecimal profit, BigDecimal rate, Rebate rebate) {
        BigDecimal res = profit.multiply(rate);
        if (rebate.getRewardAmount().compareTo(res) < 1) {
            res = rebate.getRewardAmount();
        }
        Rebate deductionRebate = CommonUtil.dataHandler(rebate, new Rebate());
        deductionRebate.setCreateTime(new Date());
        rebate.setId(null);
        int rebateNum = res.movePointRight(2).intValue();
        rebate.setRebateNum(-rebateNum);
        rebate.setRewardAmount(BigDecimal.ZERO.subtract(res));
        rebateApiRepository.save(rebate);

        //更新总表
        TotalRebate totalRebate = totalRebateApiRepository.findTotalRebateByRebateUserIdAndRebateUserType(rebate.getRewardUserId(), rebate.getRebateUserType());
        totalRebate.setUnAccountEntryTotalNum(totalRebate.getUnAccountEntryTotalNum() - rebateNum);
        totalRebateApiRepository.save(totalRebate);
        return rate;
    }

    /**
     * 会员重复购买检查
     *
     * @param uid
     */
    private void checkingRepeatBuyGift(Long uid) {
        Member member = memberRepository.findMemberByUserId(uid);
        if (member != null) {
            throw new BusinessException(ResultCode.J_CARD_BUY_REPEAT);
        }
    }

    /**
     * 将订单封装到返利业务Bo中
     *
     * @param order 订单
     * @return 订单返利Bo
     */
    private OrderRebateBo encapsulateOrderInRebateBo(Order order) {
        OrderRebateBo orderRebateBo = CommonUtil.dataHandler(order, new OrderRebateBo());
        orderRebateBo.setUserRebateBo(CommonUtil.dataHandler(order.getUser(), new UserRebateBo()));
        orderRebateBo.setOrderType(order.getOrderFrom());
        List<OrderItem> orderItems = order.getOrderItems();
        List<OrderRebateItemBo> items = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            items.add(CommonUtil.dataHandler(orderItem, new OrderRebateItemBo()));
        }

        orderRebateBo.setOrderItems(items);
        return orderRebateBo;
    }

    /**
     * round大礼包返利
     */
    private BigDecimal roundGiftRebate(BigDecimal rebate) {
        if (rebate.intValue() % 5 == 0) {
            return rebate;
        }
        BigDecimal temp;
        for (int i = 1; i < 5; i++) {
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

    private void sendMsg(BigDecimal rebatePrice, short orderType, Long uid, short rebateType,
                         short orderFrom, UserRebateBo userRebateBo, Long orderId) {
        //        if (IS_SEND_MSG) {
        if (isSendMsg) {
//            adjustUser.getPhone()
            boolean isPreBuy = Constant.ORDER_TYPE_PRE_BUY == orderType;
            boolean isGift = orderFrom == Constant.ORDER_FROM_GIFT;
            try {
                if (rebatePrice.compareTo(new BigDecimal(1)) < 0) {
                    log.info("rebatePrice less than zero no msg");
                    return;
                }

//                String testPhone = "18356508285";
//                String msgPhone = "15067187119";
                String msgPhone = userRebateBo.getPhone();
                User buyUser = userApiRepository.findOne(uid);
                if (isPreBuy) {
                    SMSUtil.sendPreBuyGiftNotify(msgPhone, buyUser.getNickName(), buyUser.getPhone(), "快速晋升通道", rebatePrice, rebateType);
                    log.info("adjustFirstOrSecondRebate IS_SEND_MSG: {} isGift: {} phone: {}", isSendMsg, isGift, userRebateBo.getPhone());
                    return;
                }

                Order order = orderApiRepository.findOne(orderId);
                OrderItem orderItem = order.getOrderItems().get(0);
                if (isGift) {
                    SMSUtil.sendGiftNotify(msgPhone, buyUser.getNickName(), buyUser.getPhone(), orderItem.getProductName(), rebatePrice, rebateType);
                } else {
                    SMSUtil.sendJdNotify(msgPhone, buyUser.getNickName(), buyUser.getPhone(), orderItem.getProductName(), rebatePrice);
                }
            } catch (Exception e) {
                log.error("adjustFirstOrSecondRebate error: {}", e);
            }
            log.info("adjustFirstOrSecondRebate IS_SEND_MSG: {} isGift: {} phone: {}", isSendMsg, isGift, userRebateBo.getPhone());
        }
    }


    @SuppressWarnings("all")
    @Override
    public JsonResult getPayOrder(PayOrderParam param) {
        OrderTypeEnum orderType = param.getOrderTypeEnum();
        String orderNum = param.getOrderNum();
        String payChannel = param.getPayChannel();
        com.ddyh.commons.result.Result result = null;
        if (orderType == OrderTypeEnum.PRE_BUY) {
            //团购订单

            PreBuyOrder preBuyOrder = preBuyOrderRepository.findByOrderNumAndPaymentState(orderNum, (short) 0);
            if (preBuyOrder == null) {
                throw new DataNotFoundException(com.sonnhe.market.pojo.result.ResultCode.DATA_NOT_EXIST_ERROR);
            }

            //更新支付渠道
            preBuyOrderRepository.updatePayChannel(payChannel, preBuyOrder.getId());

            BigDecimal payPrice = preBuyOrder.getPrice();
            result = getRequest(orderNum, payChannel, payPrice, param.getWxOpenId());
        } else if (orderType == OrderTypeEnum.EXP_CARD) {
            //体验卡订单

            ExpCardOrder item = expCardOrderRepository.findByOrderNum(orderNum);
            if (item == null) {
                throw new DataNotFoundException(com.sonnhe.market.pojo.result.ResultCode.DATA_NOT_EXIST_ERROR);
            }

            //更新支付渠道
            expCardOrderRepository.updatePayChannel(payChannel, item.getId());

            BigDecimal payPrice = item.getOrderPrice();
            result = getRequest(orderNum, payChannel, payPrice, param.getWxOpenId());

        } else {
            //大礼包、京东订单

            String orderNotPayKey = RedisConstant.ORDER_NOT_PAY_PREFIX + orderNum;
            Number number = (Number) redisUtils.get(orderNotPayKey);
            if (number == null) {
                // 订单号错误或过期
                throw new ParameterInvalidException(ResultCode.PARAM_ERROR);
            }

            long orderId = number.longValue();
            Order order = orderApiRepository.findOne(orderId);
            // 校验用户是否重复下单大礼包购买
            if (Constant.ORDER_FROM_GIFT == order.getOrderFrom()) {
                checkingRepeatBuyGift(order.getUser().getUid());
            }

            // 先查询缓存是否有已经生成的未付款订单，存在直接返回
            String payInfoKey = RedisConstant.ORDER_PAY_INFO_PREFIX + ":" + payChannel + orderNum;
            Object payOrder = redisUtils.get(payInfoKey);
            if (payOrder != null) {
                return ResultUtil.success(payOrder);
            }

            //更新支付渠道
            orderApiRepository.updatePayChannel(payChannel, orderId);

            result = getRequest(orderNum, payChannel, order.getPayPrice(), param.getWxOpenId());
            if (result.getCode().equals(com.ddyh.commons.result.ResultCode.SUCCESS.getCode())) {
                //缓存支付信息，避免未支付，再次无法支付问题
                long expireTime = Constant.ORDER_TIMER_EFFECTIVE + 2;       // 延迟于订单过期2分钟
                redisUtils.set(payInfoKey, result.getData(), expireTime, TimeUnit.MINUTES);
            }
        }
        if (result.getCode().equals(com.ddyh.commons.result.ResultCode.SUCCESS.getCode())) {
            return ResultUtil.success(result.getData());
        } else {
            return ResultUtil.error(result.getMsg());
        }
    }

    private com.ddyh.commons.result.Result getRequest(String orderNum, String payChannel, BigDecimal
            payPrice, String wxOpenId) {
        if (!Constant.isRealPay) {
            payPrice = new BigDecimal("0.01");
        }

        if (PayChannelEnum.WX_APP_PAY.getCode().equals(payChannel) || PayChannelEnum.WX_H5_PAY.getCode().equals(payChannel)) {
            WXPayParam wxparam = new WXPayParam();
            wxparam.setTradeNo(orderNum);
            wxparam.setPayChannel(payChannel);
            wxparam.setTotalFee(payPrice.movePointRight(2).intValue());
            String ip = RequestContextHolderUtil.getRequestIp();
            wxparam.setSpbillCreateIp(ip);
            wxparam.setBody("东东优汇产品");
            wxparam.setOpenId(wxOpenId);

            try {
                com.ddyh.commons.result.Result result = payFacade.getRequest(wxparam);
                log.info("getRequestresultwx=", JSON.toJSONString(result));
                return result;
            } catch (com.ddyh.commons.exception.BusinessException e) {
                throw new BusinessException(e.getMessage());
            }
        } else if (PayChannelEnum.ALI_APP_PAY.getCode().equals(payChannel)) {
            AliPayParam aliPayParam = new AliPayParam();
            aliPayParam.setTradeNo(orderNum);
            aliPayParam.setPayChannel(payChannel);
            aliPayParam.setTotalFee(payPrice);
            aliPayParam.setSubject("东东优汇产品");
            try {
                com.ddyh.commons.result.Result<String> result = payFacade.getRequest(aliPayParam);
                log.info("getRequestresultali=", JSON.toJSONString(result));
                return result;
            } catch (BusinessException e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }
}
