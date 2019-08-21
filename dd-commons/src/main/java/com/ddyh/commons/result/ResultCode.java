package com.ddyh.commons.result;

/**
 * @author: cqry2017
 * @Date: 2019/6/12 10:12
 */
@SuppressWarnings("all")
public enum ResultCode {

    SUCCESS(1, "成功"),

    FAIL(2, "失败"),

    PARAM_ERROR(1000, "参数错误"),

    USER_UNLOGIN(1010, "用户未登陆"),

    TOKEN_AUTH_FAILED(401, "token验证失败!"),

    USER_NOT_EXIT_OR_PASSWORD_ERROR(999, "用户不存在或密码错误!"),

    WECHAT_SIGNATURE_ERROR(1026, "微信签名错误");

    private Integer code;

    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
