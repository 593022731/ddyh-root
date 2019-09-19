package com.ddyh.product.admin.common.annotation;

import java.lang.annotation.*;

/**
 * @author: cqry2017
 * @Date: 2019/6/24 10:32
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperaRecord {

    /**
     * 操作类型
     **/
    String operationType();

    /**
     * 描述
     **/
    String operationName() default "";
}
