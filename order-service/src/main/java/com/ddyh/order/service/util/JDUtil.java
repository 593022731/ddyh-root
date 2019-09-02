package com.ddyh.order.service.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.order.facade.dto.jd.JDResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName JDUtil
 * @Desciption TODO
 * @Author weizheng
 * @Date 2019/1/10 14:09
 **/

public class JDUtil {

    /**
     * 请求数据结果处理
     * @param entity 请求返回结果
     * @param clazz 目标对象class
     * @param isArray 是否为集合返回结果
     * @return
     */
    public static <T, E> T dealRequestData(ResponseEntity<E> entity, Class<T> clazz, boolean isArray) {
        Object o = requestIsSuccess(entity);
        return transferObjectToNormalType(o, clazz, isArray);
    }


    /**
     * 将Object类型转换为指定的对象类型
     * @param data 带转换对象
     * @param clazz 目标对象class
     * @param isArray 是否为集合返回结果
     * @param <T> 目标对象泛型
     * @return 目标对象
     */
    private static <T> T transferObjectToNormalType(Object data, Class<T> clazz, boolean isArray) {
        if (data == null) {
            return null;
        }
        String jsonStr = JSON.toJSONString(data);
        if (isArray) {
            try{
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
            }catch (Exception e) {
                throw new BusinessException("数据转换失败");
            }
        }
        return JSONObject.parseObject(jsonStr, clazz);
    }

    private static <E> Object requestIsSuccess(ResponseEntity<E> entity) {
        int code = entity.getStatusCodeValue();
        if (code != 200) {
            throw new RemoteAccessException(ResultCode.FAIL.getMsg());
        }
        E resBody = entity.getBody();
        if (resBody instanceof JDResultDTO) {
            JDResultDTO body = (JDResultDTO) resBody;
            Integer error = body.getError();
            if (error != null && error != 0) {
                // 处理限购错误返回
                if ("10051".equals(error)) {
                    throw new RemoteAccessException(body.getMsg());
                }else {
                    // 复用hq部分可显示错误提示
                    Arrays.stream(ResultCode.values()).forEach(c -> {
                        if(c.getCode().equals(error)){
                            throw new RemoteAccessException(c.getMsg());
                        }
                    });
                }

            }
            return body.getData();
        }else if (resBody instanceof Result){
            Result body = (Result) resBody;
            Integer bodyCode = body.getCode();
            if (bodyCode != null && bodyCode != 1) {
                throw new RemoteAccessException(body.getMsg());
            }
            return body.getData();
        }
        return null;
    }
}
