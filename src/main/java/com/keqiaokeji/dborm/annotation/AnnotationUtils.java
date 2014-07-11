package com.keqiaokeji.dborm.annotation;


import com.keqiaokeji.dborm.core.ParseEntity;
import com.keqiaokeji.dborm.domain.ColumnBean;
import com.keqiaokeji.dborm.domain.TableBean;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * 支持使用Annotation标注表结构
 *
 * @author KEQIAO KEJI
 */
public class AnnotationUtils {

    private ParseEntity parseEntity;


    public AnnotationUtils() {
        parseEntity = new ParseEntity();
    }

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
                tableName = StringUtilsDborm.generateUnderlineName(entityClass.getSimpleName());
            }
            tableDomain.setTableName(tableName);
            tableDomain.setColumns(getColumnDomains(entityClass));
            tableDomain.setRelation(getRelation(entityClass));
        }
        return tableDomain;
    }

    private Map<String, ColumnBean> getColumnDomains(Class<?> entityClass) {
        Map<String, ColumnBean> columns = new HashMap<String, ColumnBean>();
        Map<String, Field> allFields = parseEntity.getEntityAllFields(entityClass);
        for (Entry<String, Field> entry : allFields.entrySet()) {
            Field field = entry.getValue();
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                ColumnBean columnDomain = getColumnDomain(field, columnAnnotation);
                String columnName = StringUtilsDborm.generateUnderlineName(field.getName());
                columns.put(columnName, columnDomain);
            }
        }
        return columns;
    }

    private ColumnBean getColumnDomain(Field field, Column anno) {
        ColumnBean columnDomain = new ColumnBean();
        columnDomain.setFieldName(field.getName());
        columnDomain.setPrimaryKey(anno.isPrimaryKey());
        columnDomain.setDefaultValue(anno.defaultValue());
        return columnDomain;
    }

    private Set<String> getRelation(Class<?> entityClass) {
        Set<String> relations = new HashSet<String>();
        Map<String, Field> allFields = parseEntity.getEntityAllFields(entityClass);
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
