package com.ddyh.product.admin.common.interceptor;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.commons.utils.ResultUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: cqry2017
 * @Date: 2019/6/12 10:33
 */
public class BaseGlobalExceptionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * 处理JSR标准的参数校验错误
     */
    protected Result handlerConstraintviolationException(ConstraintViolationException e, HttpServletRequest request) {
        String requester = getRequester(request);
        LOGGER.info("handlerConstraintviolationException start, requester [{}], uri: [{}], caused by: [{}]", requester, request.getRequestURI(), e);
        List<String> msgList = new ArrayList<>();
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        for (ConstraintViolation<?> next : constraintViolations) {
            msgList.add(next.getMessageTemplate());
        }
        return ResultUtil.error(ResultCode.PARAM_ERROR, msgList);
    }

    /**
     * 处理Hibernate扩展校验注解异常
     */
    protected Result handlerMethodArgumentnotvalidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String requester = getRequester(request);
        LOGGER.info("handlerMethodArgumentnotvalidException start, requester [{}], uri: [{}], caused by: [{}]", requester, request.getRequestURI(), e);
        List<String> msgList = dealBindResult(e.getBindingResult());
        return ResultUtil.error(ResultCode.PARAM_ERROR, msgList);
    }

    /**
     * 处理数据绑定异常
     */
    protected Result handlerBindException(BindException e, HttpServletRequest request) {
        String requester = getRequester(request);
        LOGGER.info("handlerBindException start, requester [{}], uri: [{}], caused by: [{}]", requester, request.getRequestURI(), e);
        List<String> msgList = dealBindResult(e.getBindingResult());
        return ResultUtil.error(ResultCode.PARAM_ERROR, msgList);
    }


    /**
     * 处理自定义异常
     */
    protected ResponseEntity<Result> handlerBusinessException(BusinessException e, HttpServletRequest request) {
        String requester = getRequester(request);
        LOGGER.info("handlerBusinessException start, requester [{}], uri: [{}], exception: [{}], caused by: [{}]", requester, request.getRequestURI(), e.getClass(), e.getMsg());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResultUtil.error(e.getResultCode()));
    }

    /**
     * 处理bindingResult
     */
    private List<String> dealBindResult(BindingResult bindingResult) {
        List<String> msgList = new ArrayList<>();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError objectError : allErrors) {
            msgList.add(objectError.getDefaultMessage());
        }
        return msgList;
    }

    /**
     * 处理未认证异常
     */
    protected Result HandlerUnauthorizedException(BusinessException e, HttpServletRequest request) {
        String requester = getRequester(request);
        LOGGER.info("HandlerMethodArgumentNotValidException start, requester [{}], uri: [{}], caused by: [{}]", requester, request.getRequestURI(), e);
        return ResultUtil.error(ResultCode.USER_UNLOGIN);
    }

    /**
     * 获取请求者
     *
     * @param request
     * @return
     */
    private String getRequester(HttpServletRequest request) {
        String token = request.getHeader("token");
        String requester = "unknown";
        if (!Strings.isNullOrEmpty(token) && token.contains(".")) {
            String[] split = token.split(".");
            if (split.length == 2) {
                requester = split[1];
            }
        }
        return requester;
    }
}
