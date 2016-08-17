package org.dborm.web;


import org.dborm.core.annotation.Column;
import org.dborm.core.domain.BaseDomain;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 抽取表中的公共字段
 * Created by shk
 */
public class AppDomain extends BaseDomain implements Serializable {

    /**
     * 表的主键ID（每个表尽可能都有主键,而且最好主键字段的名字是相同的）
     */
    @Column(isPrimaryKey = true)
    String id;

    @Column
    String createBy;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    Date createTime;

    @Column
    String updateBy;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    Date updateTime;


    @Column(defaultValue = "0")
    Integer deleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
