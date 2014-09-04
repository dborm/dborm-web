package cn.cocho.dborm.annotation;


import cn.cocho.dborm.core.EntityResolver;
import cn.cocho.dborm.domain.ColumnBean;
import cn.cocho.dborm.domain.TableBean;
import cn.cocho.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 支持使用Annotation标注表结构
 *
 * @author KEQIAO KEJI
 */
public class AnnotationUtils {

    /**
     * 通过类获得该类的注解描述信息
     *
     * @param entityClass 类对象
     * @return 注解描述信息或者null
     */
    public TableBean getTableDomain(Class<?> entityClass) {
        TableBean tableDomain = null;
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            tableDomain = new TableBean();
            String tableName = tableAnnotation.tableName();
            if (StringUtilsDborm.isEmpty(tableName)) {
                tableName = StringUtilsDborm.humpToUnderlineName(entityClass.getSimpleName());
            }
            tableDomain.setTableName(tableName);
            tableDomain.setColumns(getColumnDomains(entityClass));
            tableDomain.setRelation(getRelation(entityClass));
        }
        return tableDomain;
    }

    private Map<String, ColumnBean> getColumnDomains(Class<?> entityClass) {
        Map<String, ColumnBean> columns = new HashMap<String, ColumnBean>();
        Map<String, Field> allFields = EntityResolver.getEntityAllFields(entityClass);//此处不能从Cache类的缓存里取数据，否则会出现死循环
        for (Entry<String, Field> entry : allFields.entrySet()) {
            Field field = entry.getValue();
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                ColumnBean columnDomain = getColumnDomain(field, columnAnnotation);
                String columnName = StringUtilsDborm.humpToUnderlineName(field.getName());
                columns.put(columnName, columnDomain);
            }
        }
        return columns;
    }

    private ColumnBean getColumnDomain(Field field, Column column) {
        ColumnBean columnDomain = new ColumnBean();
        columnDomain.setFieldName(field.getName());
        columnDomain.setPrimaryKey(column.isPrimaryKey());
        columnDomain.setDefaultValue(column.defaultValue());
        return columnDomain;
    }

    private Set<String> getRelation(Class<?> entityClass) {
        Set<String> relations = new HashSet<String>();
        Map<String, Field> allFields =  EntityResolver.getEntityAllFields(entityClass);//此处不能从Cache类的缓存里取数据，否则会出现死循环
        for (Entry<String, Field> entry : allFields.entrySet()) {
            Field field = entry.getValue();
            Relation relation = field.getAnnotation(Relation.class);
            if (relation != null) {
                relations.add(field.getName());
            }
        }
        return relations;
    }


}
