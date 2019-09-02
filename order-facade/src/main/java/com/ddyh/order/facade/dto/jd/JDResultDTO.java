package com.ddyh.order.facade.dto.jd;

import java.io.Serializable;

/**
 * 京东请求resultDTO
 */
public class JDResultDTO implements Serializable {

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
