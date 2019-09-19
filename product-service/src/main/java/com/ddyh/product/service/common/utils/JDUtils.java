package com.ddyh.product.service.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @ClassName JDUtils
 * @Desciption 京东工具类
 * @Author weizheng
 * @Date 2018/12/26 16:24
 **/
public class JDUtils {

    private static Logger log = LoggerFactory.getLogger(JDUtils.class);

    static final int VENDER_ID = 593;     // 京东渠道id

    static final String APP_KEY = "300ba72aa1a65dc93d3e63634bb91e97";

    static final String USER_NAME = "sh2019";

    static final String PASS_WORD = "0d712a05b2eb8c4282b19dd4bdec145c";

    /**
     * 处理get请求
     * 注：转换为list类型时，通过的是自定义包装类（里面放入list参数，变量名必须为list）
     *
     * @param url          请求地址
     * @param restTemplate rest请求对象
     * @param clazz        目标对象class
     * @param isArray      是否为集合返回结果
     * @param object       请求参数
     */
    public static <T> T dealGetRequest(String url, RestTemplate restTemplate, Class<T> clazz,
                                       boolean isArray, Object object) {
        String param;
        try {
            param = dealGetParam(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("参数拼接失败");
        }
        url = getUrl(url);
        url += param;
        log.info("url:" + url);
        ResponseEntity<JDResult> entity = restTemplate.getForEntity(url, JDResult.class);
        return dealRequestData(entity, clazz, isArray);
    }

    private static String getUrl(String url) {
        String myUrl = url;
        if (!StringUtils.isEmpty(myUrl)) {
            if (myUrl.contains("?")) {
                myUrl += "&";
            } else {
                myUrl += "?";
            }
            myUrl += "appKey=" + APP_KEY;
            myUrl += "&venderId=" + VENDER_ID;
            myUrl += "&userName=" + USER_NAME;
            myUrl += "&passWord=" + PASS_WORD;
        }
        return myUrl;
    }

    /**
     * 处理非对象参数get请求
     * 注：转换为list类型时，通过的是自定义包装类（里面放入list参数，变量名必须为list）
     *
     * @param url          请求地址
     * @param restTemplate restTemplate
     * @param clazz        目标对象class
     * @param isArray      是否为集合返回结果
     * @param objects      参数
     */
    public static <T> T dealSingleParamGetRequest(String url, RestTemplate restTemplate, Class<T> clazz,
                                                  boolean isArray, Object... objects) {
        url = getUrl(url);
        log.info("url-1:" + url);
        ResponseEntity<JDResult> entity = restTemplate.getForEntity(url, JDResult.class, objects);
        return dealRequestData(entity, clazz, isArray);
    }

    /**
     * 请求数据结果处理
     *
     * @param entity  请求返回结果
     * @param clazz   目标对象class
     * @param isArray 是否为集合返回结果
     */
    public static <T> T dealRequestData(ResponseEntity<JDResult> entity, Class<T> clazz, boolean isArray) {
        JDResult body = requestIsSuccess(entity);
        return transferObjectToNormalType(body.getData(), clazz, isArray);
    }

    /**
     * 将Object类型转换为指定的对象类型
     *
     * @param data    带转换对象
     * @param clazz   目标对象class
     * @param isArray 是否为集合返回结果
     * @param <T>     目标对象泛型
     * @return 目标对象
     */
    private static <T> T transferObjectToNormalType(Object data, Class<T> clazz, boolean isArray) {
        if (data == null) {
            throw new NullPointerException("转型数据为空");
        }
        String jsonStr = JSON.toJSONString(data);
        if (isArray) {
            try {
                T t = clazz.newInstance();
                // 获取数据集合接收属性
                Field listField = clazz.getDeclaredField("list");
                // 获取数据类型属性
                Field typeField = clazz.getDeclaredField("type");
                Class<?> type = typeField.getType();
                List<?> list = JSONArray.parseArray(jsonStr, type);
                listField.setAccessible(true);
                listField.set(t, list);
                return t;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("数据转换失败");
            }
        }
        return JSONObject.parseObject(jsonStr, clazz);
    }

    public static JDResult requestIsSuccess(ResponseEntity<JDResult> entity) {
        int code = entity.getStatusCodeValue();
        if (code != 200) {
            throw new RuntimeException("远程请求建立失败");
        }
        JDResult body = entity.getBody();
        Integer error = body.getError();

        if (error != 0) {
            String msg = "error = " + error + ", msg = " + body.getMsg();
            throw new RuntimeException("远程操作失败");
        }
        return body;
    }

    /**
     * get请求参数处理
     */
    private static String dealGetParam(Object object) throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();
//        sb.append("?");
        Class<?> clazz = object.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            Object o = field.get(object);
            if (o == null) {
                continue;
            }
            sb.append("&");
            sb.append(field.getName());
            sb.append("=");
            sb.append(o);
        }
//        String res = sb.toString();
//        if (res.length() > 0) {
//            res = res.substring(0, res.length() - 1);
//        }
        return sb.toString();
    }

    public static void main(String[] args) throws IllegalAccessException {
    }
}
