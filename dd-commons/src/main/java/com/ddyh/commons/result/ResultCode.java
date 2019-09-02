package com.ddyh.commons.result;

/**
 * 通用code
 */
@SuppressWarnings("all")
public enum ResultCode {

    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    REPEAT_COMMIT(3, "重复提交"),
    TOKEN_AUTH_FAILED(401, "token验证失败!"),

    PARAM_ERROR(444, "参数错误"),

    //订单系统code，从100001 开始
    STOCK_NOT_ENOUGH(100001, "库存不足"),
    PRICE_CHANGED(20054, "价格有变化，请确认后下单"),

    //商品系统code，从200001 开始


    //用户系统code，从300001 开始


    //返利系统code，从400001 开始
    ;

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
