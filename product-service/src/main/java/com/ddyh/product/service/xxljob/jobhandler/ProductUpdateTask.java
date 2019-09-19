/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: UploadTask
 * Author:   Liubing
 * Date:     2018/5/21 10:56
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.ddyh.product.service.xxljob.jobhandler;

import com.ddyh.product.facade.facade.ProductFacade;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 商品增量更新job
 */
@JobHandler
@Component
public class ProductUpdateTask extends IJobHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ProductFacade productFacade;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        logger.info("ProductUpdateTask start");
        productFacade.updateDbProdcut();
        logger.info("ProductUpdateTask end");
        return SUCCESS;
    }
}