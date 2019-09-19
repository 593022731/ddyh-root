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

import com.ddyh.product.service.common.utils.WeixinUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 获取微信token
 */
@JobHandler
@Component
public class WeiXinGetTokenTask extends IJobHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 微信token2小时过期，生产用30分钟查询一次，更新本地缓存，2台机器轮询调用，保证两台机器不会token过期
     *
     * @param s
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        logger.info("getwxtoken start");
        WeixinUtil.init();
        logger.info("getwxtoken end");
        return SUCCESS;
    }
}