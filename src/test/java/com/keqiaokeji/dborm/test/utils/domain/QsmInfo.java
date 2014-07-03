package com.keqiaokeji.dborm.test.utils.domain;

import com.keqiaokeji.dborm.annotation.Column;
import com.keqiaokeji.dborm.annotation.Table;

@Table
public class QsmInfo {

    /**
     * 关联到的问题id
     */
    @Column
    private String questionId;

    /**
     * 问题选项的内容
     */
    @Column
    private String content;

    @Column
    private String userId;


    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
