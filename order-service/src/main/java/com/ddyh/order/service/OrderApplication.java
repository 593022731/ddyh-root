package com.ddyh.order.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author weihui  2018年10月09日 10:26
 **/
@EnableDubbo
@SpringBootApplication
//@EnableTransactionManagement(proxyTargetClass = true)
//@MapperScan("com.ddyh.pay.dao.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
