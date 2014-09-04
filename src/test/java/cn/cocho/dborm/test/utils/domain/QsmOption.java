package cn.cocho.dborm.test.utils.domain;

import cn.cocho.dborm.annotation.Column;
import cn.cocho.dborm.annotation.Table;

@Table
public class QsmOption {

    /**
     * 问题选项id
     */
    @Column(isPrimaryKey = true)
    private String optionId;

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

    /**
     * 选项的排序
     */
    @Column
    private Float showOrder;

    /**
     * 用于标示当前题干的附件个数。
     */
    @Column
    private Integer attachmentCount = 0;

    @Column
    private String userId;

//	@Relation("附件")
//	private List<QsmAttachment> attachments;

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

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

    public Float getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(Float showOrder) {
        this.showOrder = showOrder;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
