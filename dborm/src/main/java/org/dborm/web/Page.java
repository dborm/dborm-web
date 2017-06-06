package org.dborm.web;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用JQGrid分页时用到的对象
 */
public class Page<T> {


    /**
     * 当前页是第几页
     */
    private int pageNo = 1;

    /**
     * 总页数
     */
    private long totalPage = 0;

    /**
     * 总记录数
     */
    private long records = 0;

    /**
     * 每页显示的条数
     */
    private int rows = 10;

    /**
     * 记录集
     */
    private List<T> dataRows = new ArrayList<T>();

    private Object data;


    public int getFirstRecord() {
        return (pageNo - 1) * rows;
    }


    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public long getRecords() {
        return records;
    }

    public void setRecords(long records) {
        this.records = records;
        totalPage = records / rows;
        if (records % rows > 0) {
            totalPage = totalPage + 1;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<T> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<T> dataRows) {
        this.dataRows = dataRows;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

