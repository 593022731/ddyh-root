package com.ddyh.product.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author weihui  2018年10月09日 10:26
 **/

@EnableDubbo
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan("com.product.dao.mapper")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
