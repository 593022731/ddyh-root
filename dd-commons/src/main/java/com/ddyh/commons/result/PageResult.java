package com.ddyh.commons.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象
 * @author: weihui
 * @Date: 2019/6/10 11:41
 */
public class PageResult<T> implements Serializable {

    /*总条数*/
    private Long totalCount;
    /*结果集*/
    private List<T> dataList;

    public PageResult() {}

    public PageResult(Long totalCount, List<T> dataList) {
        this.totalCount = totalCount;
        this.dataList = dataList;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public List<T> getDataList() {
        return dataList;
    }

}
