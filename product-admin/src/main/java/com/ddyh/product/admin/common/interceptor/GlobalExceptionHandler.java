package com.ddyh.product.admin.common.interceptor;

import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;

/**
 * @author: cqry2017
 * @Date: 2019/6/12 10:22
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Override
    public Result handlerConstraintviolationException(ConstraintViolationException e, HttpServletRequest request) {
        return super.handlerConstraintviolationException(e, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Override
    public Result<List<String>> handlerMethodArgumentnotvalidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        return super.handlerMethodArgumentnotvalidException(e, request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Override
    public Result handlerBindException(BindException e, HttpServletRequest request) {
        return super.handlerBindException(e, request);
    }


    @ExceptionHandler(BusinessException.class)
    @Override
    public ResponseEntity<Result> handlerBusinessException(BusinessException e, HttpServletRequest request) {
        return super.handlerBusinessException(e, request);
    }

    @ExceptionHandler(BusinessException.class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @Override
    public Result HandlerUnauthorizedException(BusinessException e, HttpServletRequest request) {
        return super.HandlerUnauthorizedException(e, request);
    }
}
