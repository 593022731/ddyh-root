package com.ddyh.product.service.common.utils;


/**
 * @ClassName JDResult
 * @Desciption 京东结果bean
 * @Author weizheng
 * @Date 2018/12/26 14:19
 **/
public class JDResult {

    private Integer error;

    private String msg;

    private Object data;

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
