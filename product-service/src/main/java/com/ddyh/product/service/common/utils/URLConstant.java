package com.ddyh.product.service.common.utils;

/**
 * @ClassName URLConstant
 * @Desciption 地址常量
 * @Author weizheng
 * @Date 2018/12/22 12:21
 **/
public class URLConstant {

    //        private static final String JD_MARKET_PREFIX = "https://tcmallwx.tcvideo.com.cn/v1";
    private static final String JD_MARKET_PREFIX = "https://extintsh.tcvideo.com.cn/v1";
    // 认证参数
    private static final String JD_AUTH_SUFFER = "";

    // 省级列表
    public static final String PROVINCE_LIST = JD_MARKET_PREFIX + "/address/provincelist";

    // 城市列表
    public static final String CITY_LIST = JD_MARKET_PREFIX + "/address/citylist";

    // 市/县级列表
    public static final String COUNTY_LIST = JD_MARKET_PREFIX + "/address/countylist";

    // 镇/乡级列表
    public static final String TOWN_LIST = JD_MARKET_PREFIX + "/address/townlist";

    // jd库存
    public static final String STOCK_DETAIL = JD_MARKET_PREFIX + "/bms/stock/detail" + JD_AUTH_SUFFER;

    // jd产品分类列表
    public static final String CATEGORY_LIST = JD_MARKET_PREFIX + "/product/cate/list";

    // jd产品列表
    public static final String PRODUCT_LIST = JD_MARKET_PREFIX + "/product/list";

    // jd全品类商品列表
    public static final String PRODUCT_LIST_ALL = JD_MARKET_PREFIX + "/product/syncList";

    // jd产品详情
    public static final String PRODUCT_DETAIL = JD_MARKET_PREFIX + "/product/detailPc";

    public static final String SIMILAR_PRODUCT = JD_MARKET_PREFIX + "/product/similarGoods";

    public static final String ORDER_SYSTEM_PREFIX = "http://localhost:8080";

    public static final String USER_CHECKING = ORDER_SYSTEM_PREFIX + "/api/user/{phone}/checking";

    // jd增量同步商品更新信息
    public static final String PRODUCT_UPDATE = JD_MARKET_PREFIX + "/product/update";

    // jd删除增量同步商品数据接口(post请求，要加上后面的参数JD_AUTH_SUFFER)
    public static final String PRODUCT_REMOVEUPDATE = JD_MARKET_PREFIX + "/product/removeUpdate" + JD_AUTH_SUFFER;

    public static final String PRODUCT_AREA_LIMIT = JD_MARKET_PREFIX + "/bms/vender/check/areaLimit";

    public static final String PRODUCT_CAN_SALE = JD_MARKET_PREFIX + "/bms/vender/check/sku";
    /**
     * 1.11 实时查询单个商品详情
     */
    public static final String PRODUCT_DETAIL_REAL_TIME = JD_MARKET_PREFIX + "/product/vender/detail";
}
