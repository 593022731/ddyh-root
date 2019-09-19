package com.ddyh.product.admin.common.interceptor;


import com.alibaba.dubbo.config.annotation.Reference;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ddyh.commons.exception.BusinessException;
import com.ddyh.commons.result.ResultCode;
import com.ddyh.product.admin.common.annotation.PassToken;
import com.ddyh.product.admin.controller.BaseController;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.facade.BackUserFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 17:23
 */
public class UserAuthenticationInterceptor extends BaseController implements HandlerInterceptor {

    @Reference
    private BackUserFacade backUserFacade;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String token = request.getHeader("token");
        if (o instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) o;
            // 判断类或者方法上是否存在需要用户身份认证的注解
            boolean hasClassAnnotation = handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(PassToken.class);
            boolean hasMethodAnnotation = handlerMethod.hasMethodAnnotation(PassToken.class);
            if (hasClassAnnotation || hasMethodAnnotation) {
                return true;
            }
        }

        if (StringUtils.isEmpty(token)) {
//            throw new UnauthorizedException(ResultCode.USER_UNLOGIN);
            return true;
        }
        JdBackUserDTO backUserDto = getJdBackUser(token);
        if (backUserDto == null) {
            throw new BusinessException(ResultCode.TOKEN_AUTH_FAILED);
        }
        // 验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(backUserDto.getPassword())).build();
        try {
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new BusinessException(ResultCode.TOKEN_AUTH_FAILED);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

}
