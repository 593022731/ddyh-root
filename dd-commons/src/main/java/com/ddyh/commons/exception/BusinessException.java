package com.ddyh.commons.exception;


import com.ddyh.commons.result.ResultCode;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    protected Integer code;

    protected String msg;

    protected ResultCode resultCode;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
        this.msg = message;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getMsg());
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public BusinessException(ResultCode resultCode, String msg) {
        this(msg);
        this.resultCode = resultCode;
        this.code = resultCode.getCode();
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
