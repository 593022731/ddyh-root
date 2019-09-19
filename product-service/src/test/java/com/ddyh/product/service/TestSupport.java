package com.ddyh.product.service;

import com.alibaba.fastjson.JSON;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 类TestSupport.java的实现描述：单元测试支持类
 *
 * @author weihui 2018/10/25 10:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProductApplication.class)
public class TestSupport {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 单元测试打印
     *
     * @param obj
     * @author weihui 2017年7月27日 上午10:04:12
     */
    protected void printLog(Object obj) {
        logger.info(JSON.toJSONString(obj));
    }

}
