package org.dborm.web;


import org.dborm.core.annotation.Column;
import org.dborm.core.domain.BaseDomain;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 抽取表中的公共字段
 * Created by shk
 */
public class AppDomain extends BaseDomain {

    /**
     * 表的主键ID（每个表尽可能都有主键,而且最好主键字段的名字是相同的）
     */
    @Column(isPrimaryKey = true)
    private String id;

    @Column
    private String createUserId;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    @Column
    private String modifyUserId;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifyTime;


    @Column(defaultValue = "0")
    private Integer deleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getModifyUserId() {
        return modifyUserId;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
