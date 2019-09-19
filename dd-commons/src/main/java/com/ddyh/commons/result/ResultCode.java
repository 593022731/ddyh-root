package com.ddyh.commons.result;

/**
 * 通用code
 */
@SuppressWarnings("all")
public enum ResultCode {

    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    REPEAT_COMMIT(3, "重复提交"),
    USER_UNLOGIN(1010, "用户未登陆"),
    TOKEN_AUTH_FAILED(401, "token验证失败!"),
    USER_NOT_EXIT_OR_PASSWORD_ERROR(999, "用户不存在或密码错误!"),
    WECHAT_SIGNATURE_ERROR(1026, "微信签名错误"),
    PARAM_ERROR(444, "参数错误"),

    //订单系统code，从100001 开始
    STOCK_NOT_ENOUGH(100001, "库存不足"),
    PRICE_CHANGED(20054, "价格有变化，请确认后下单"),

    //商品系统code，从200001 开始
    PRICE_BETWEEN_NOT_OK(200001, "京东推荐价减去会员价 需大于等于 ");

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
