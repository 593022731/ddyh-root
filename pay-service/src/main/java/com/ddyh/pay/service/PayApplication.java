package com.ddyh.pay.service;

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
@MapperScan("com.ddyh.pay.dao.mapper")
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
