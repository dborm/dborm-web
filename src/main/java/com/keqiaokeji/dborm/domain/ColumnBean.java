package com.keqiaokeji.dborm.domain;

/**
 * 列模型
 *
 * @author KEQIAO KEJI
 * @time 2013年9月22日 @下午2:52:37
 */
public class ColumnBean {

    /**
     * 属性名（必须有）
     */
    private String fieldName;

    /**
     * 是否作为主键，支持联合主键
     */
    private boolean isPrimaryKey;

    /**
     * 默认值(字符串"null"或null相当于没有默认值)
     */
    private Object defaultValue;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }


}
