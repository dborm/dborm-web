package cn.cocho.dborm.test.utils.domain;

import cn.cocho.dborm.annotation.Column;
import cn.cocho.dborm.annotation.Relation;
import cn.cocho.dborm.annotation.Table;

import java.util.Date;
import java.util.List;

@Table
public class LoginUser {

    @Column(isPrimaryKey = true)
    public String id;

    @Column(isPrimaryKey = true)
    private String userId;

    @Column
    private String userName;

    @Column
    private Integer age;

    @Column
    private Boolean isBoy;

    @Column
    private Date birthday;

    private String des;

    /**
     * 关联到的问题id
     */
    private String questionId;

    /**
     * 问题选项的内容
     */
    private String content;

    /**
     * 登录次数（默认值为0）
     */
    @Column(defaultValue = "0")
    private Integer loginNum;

    @Relation
    private List<QsmOption> qsmOptionList;

    @Relation
    private List<QsmInfo> qsmInfoList;


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public Integer getAge() {
        return age;
    }


    public void setAge(Integer age) {
        this.age = age;
    }


    public Boolean getIsBoy() {
        return isBoy;
    }


    public void setIsBoy(Boolean isBoy) {
        this.isBoy = isBoy;
    }


    public Date getBirthday() {
        return birthday;
    }


    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }


    public String getDes() {
        return des;
    }


    public void setDes(String des) {
        this.des = des;
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


    public List<QsmOption> getQsmOptionList() {
        return qsmOptionList;
    }


    public void setQsmOptionList(List<QsmOption> qsmOptionList) {
        this.qsmOptionList = qsmOptionList;
    }


    public List<QsmInfo> getQsmInfoList() {
        return qsmInfoList;
    }


    public void setQsmInfoList(List<QsmInfo> qsmInfoList) {
        this.qsmInfoList = qsmInfoList;
    }


    public Integer getLoginNum() {
        return loginNum;
    }


    public void setLoginNum(Integer loginNum) {
        this.loginNum = loginNum;
    }


}
