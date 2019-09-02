package com.ddyh.order.service.util;

/**
 * 请求京东地址
 */
public class JDUrlUtil {

    // 请求地址域名
    private static final String JD_MARKET_PREFIX = "https://extintsh.tcvideo.com.cn/v1";

    // 认证参数
    private static final String JD_AUTH_SUFFER = "?appKey=300ba72aa1a65dc93d3e63634bb91e97&venderId=593&userName=sh2019&passWord=0d712a05b2eb8c4282b19dd4bdec145c";

    // 查询运费
    public static final String JD_FREIGHT = JD_MARKET_PREFIX + "/bms/order/freight" + JD_AUTH_SUFFER;

    // 查询是否支持售后
    public static final String JD_IS_SUPPORT_AFTER_SALE = JD_MARKET_PREFIX + "/vender/afterSale/isCan" + JD_AUTH_SUFFER;

    // 查询商品售后服务类型
    public static final String JD_PRODUCT_AFTER_SALE_TYPE = JD_MARKET_PREFIX + "/vender/afterSale/service" + JD_AUTH_SUFFER;

    // 申请售后
    public static final String JD_APPLY_AFTER_SALE = JD_MARKET_PREFIX + "/vender/afterSale/applyAfterSale" + JD_AUTH_SUFFER;

    // 取消售后
    public static final String JD_CANCEL_AFTER_SALE = JD_MARKET_PREFIX + "/vender/afterSale/cancelService" + JD_AUTH_SUFFER;

    // 售后详情
    public static final String JD_AFTER_SALE_DETAIL = JD_MARKET_PREFIX + "/vender/afterSale/detail" + JD_AUTH_SUFFER;

    // 售后详情
    public static final String JD_AFTER_SALE_RETURN_TYPE = JD_MARKET_PREFIX + "/vender/afterSale/returnType" + JD_AUTH_SUFFER;

    // 查询售后信息
    public static final String JD_AFTER_SALE_INFO = JD_MARKET_PREFIX + "/vender/afterSale/list" + JD_AUTH_SUFFER;

    // 查询配送信息
    public static final String JD_ORDER_TRACK = JD_MARKET_PREFIX + "/bms/vender/order/track" + JD_AUTH_SUFFER;

    // 请求下单
    public static final String JD_CREATE_ORDER = JD_MARKET_PREFIX + "/bms/vender/order" + JD_AUTH_SUFFER;

    // 确认支付
    public static final String JD_ORDER_PAY_NOTIFY = JD_MARKET_PREFIX + "/bms/vender/order/notify" + JD_AUTH_SUFFER;

    // 订单详情查询（订单状态）
    public static final String JD_ORDER_DETAIL = JD_MARKET_PREFIX + "/bms/vender/order/jddetail" + JD_AUTH_SUFFER;

}
