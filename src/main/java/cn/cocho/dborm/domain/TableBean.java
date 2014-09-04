package cn.cocho.dborm.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 表模型
 *
 * @author KEQIAO KEJI
 * @time 2013年9月22日 @下午2:52:23
 */
public class TableBean {

    /**
     * schema信息对应的类路径
     */
    private String classPath;

    /**
     * 表的名称（可以忽略该属性，如果为空则自动将驼峰格式的类名转换为下划线格式的表名）
     */
    private String tableName;

    /**
     * 表中包含的列集合，该属性为缓存记录使用，非配置文件属性<br>
     * 键：列名 值：列的属性描述
     */
    private Map<String, ColumnBean> columns = new HashMap<String, ColumnBean>();

    /**
     * 级联操作标识，用于深度操作，该属性为缓存记录使用，非配置文件属性<br>
     * 值为深度操作的属性名称
     */
    private Set<String> relation = new HashSet<String>();


    public Map<String, ColumnBean> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, ColumnBean> columns) {
        this.columns = columns;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<String> getRelation() {
        return relation;
    }

    public void setRelation(Set<String> relation) {
        this.relation = relation;
    }


}
