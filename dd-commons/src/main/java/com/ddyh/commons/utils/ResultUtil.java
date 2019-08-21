package com.ddyh.commons.utils;

import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;


public class ResultUtil {
    /**
     * 获取成功结果，不含参数
     * @return
     */
    public static Result success() {
        return new Result(ResultCode.SUCCESS);
    }

    /**
     * 获取成功返回结果，含参数
     * @param data  返回数据
     * @return
     */
    public static <T> Result<T> success(T data) {
        Result<T> Result = new Result(ResultCode.SUCCESS);
        Result.setData(data);
        return Result;
    }

    /**
     * 获取失败的返回结果，不含参数
     * @param resultCode 结果码
     * @return Result
     */
    public static Result error(ResultCode resultCode) {
        Result result = new Result(resultCode);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result(ResultCode.FAIL.getCode(), msg);
        return result;
    }

    /**
     * 获取失败的返回结果
     * @param resultCode    结果码
     * @param data  返回数据
     * @return
     */
    public static <T> Result<T> error(ResultCode resultCode, T data){
        Result<T> Result = new Result<>(resultCode);
        Result.setData(data);
        return Result;
    }
}
