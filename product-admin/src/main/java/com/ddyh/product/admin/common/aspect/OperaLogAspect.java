package com.ddyh.product.admin.common.aspect;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ddyh.product.admin.common.annotation.OperaRecord;
import com.ddyh.product.admin.common.util.RequestUtil;
import com.ddyh.product.admin.controller.BaseController;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.dto.OperaLogDto;
import com.ddyh.product.facade.facade.BackUserFacade;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author: cqry2017
 * @Date: 2019/6/24 14:45
 */
@Aspect
@Component
public class OperaLogAspect extends BaseController {
    private final static Logger LOGGER = LoggerFactory.getLogger(OperaLogAspect.class);
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Reference
    private BackUserFacade backUserFacade;

    @Pointcut("@annotation(com.product.admin.common.annotation.OperaRecord)")
    public void aspectLog() {

    }

    @Before("aspectLog()")
    public void doBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        HttpServletRequest request = getRequest();
        if (request == null) {
            return;
        }
        // 打印请求相关参数
        LOGGER.info("========================================== Start ==========================================");
        // 打印请求 url
        LOGGER.info("URL            : {}", request.getRequestURL().toString());
        // 打印 Http method
        LOGGER.info("HTTP Method    : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        LOGGER.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        LOGGER.info("IP             : {}", RequestUtil.getClientIpAddr(request));
        // 打印请求入参
        LOGGER.info("Request Args   : {}", JSON.toJSONString(joinPoint.getArgs()));

        OperaLogDto log = null;
        try {
            log = getLog(joinPoint, request);
        } catch (Exception e) {
            LOGGER.error("OperaLogAspect getLog error!");
        }
        if (log != null) {
            backUserFacade.saveOperaLog(log);
        }
    }

    private OperaLogDto getLog(JoinPoint joinPoint, HttpServletRequest request) throws Exception {
        JdBackUserDTO userDto = getJdBackUser(getToken(request));
        if (userDto == null) {
            return null;
        }
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        String params = null;
        int maxLen = 200;
        if (arguments != null && arguments.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (Object argument : arguments) {
                if (StringUtils.isNotEmpty(sb.toString())) {
                    sb.append(";");
                }
                sb.append(argument);
            }
            params = sb.toString();
            if (params.length() > maxLen) {
                params = params.substring(0, maxLen);
            }
        }
        Class targetClass;
        targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        String operationName = "";
        String operationType = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (arguments != null && clazzs.length == arguments.length && method.getAnnotation(OperaRecord.class) != null) {
                    operationName = method.getAnnotation(OperaRecord.class).operationName();
                    operationType = method.getAnnotation(OperaRecord.class).operationType();
                    break;
                }
            }
        }

        OperaLogDto log = new OperaLogDto();
        log.setLogType(1);
        log.setDescription(operationName);
        log.setMethod(methodName);
        log.setIpAddr(RequestUtil.getClientIpAddr(request));
        log.setOperaUser(userDto.getUserName());
        log.setParams(params);
        log.setType(operationType);
        return log;
    }


    @Around("aspectLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
//        LOGGER.info("Response Args  : {}", JSON.toJSONString(result));
        // 执行耗时
        LOGGER.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime.get());
        startTime.remove();
        LOGGER.info("=========================================== End ===========================================");
        return result;
    }

    @AfterThrowing(pointcut = "aspectLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            OperaLogDto log = getLog(joinPoint, getRequest());
            if (log != null) {
                log.setErrorCode(e.getClass().getName());
                log.setErrorMsg(e.getMessage());
                backUserFacade.saveOperaLog(log);
            }
        } catch (Exception ex) {
            LOGGER.error("OperaLogDtoAspect doAfterThrowing error:", ex);
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }
}
