package com.ddyh.commons.result;

import java.io.Serializable;

/**
 * 通用result
 * @author: weihui
 * @Date: 2019/6/10 11:33
 */
public class Result<T> implements Serializable {

    /*响应码*/
    private Integer code = ResultCode.SUCCESS.getCode();
    /*响应消息*/
    private String msg = ResultCode.SUCCESS.getMsg();
    /*响应对象*/
    private T data;

    public Result(){
    }

    public Result(T data){
        this.data = data;
    }

    public Result(ResultCode resultCode){
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }


    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
