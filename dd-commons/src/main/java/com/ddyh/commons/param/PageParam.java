package com.ddyh.commons.param;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 分页参数
 * @author: weihui
 * @Date: 2019/6/10 11:46
 */
public class PageParam implements Serializable {

    // 当前页
    private Integer currentPage;

    // 每页大小
    private Integer pageSize;

    public Integer getCurrentPage() {
        if (currentPage == null || StringUtils.isEmpty(currentPage.toString())) {
            return 1;
        }
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        if (pageSize == null || StringUtils.isEmpty(pageSize.toString())) {
            return 10;
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
