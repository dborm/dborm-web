package com.keqiaokeji.dborm.annotation;


import com.keqiaokeji.dborm.core.Cache;
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
public class DbormAnnotationInit {

    private List<String> scanPackageList;
    private Set<Class<?>> entityClasses = new HashSet<Class<?>>();
    private ParseEntity parseEntity;


    public DbormAnnotationInit(){
        parseEntity = new ParseEntity();
    }

    /**
     * 初始化使用Annotation标注的表结构对象
     *
     * @return 初始化之后的所有结果集
     * @author KEQIAO KEJI
     */
    public Map<String, TableBean> initSchema() {
        Map<String, TableBean> annotationSchemas = new HashMap<String, TableBean>();
        entityClasses.addAll(AnnotationScan.scanClassByPackages(scanPackageList));
        if (entityClasses.size() > 0) {
            for (Class<?> entityClass : entityClasses) {
                if (Cache.getTablesCache(entityClass.getName()) == null) {//如果缓存中不存在则使用注解初始化
                    TableBean tableDomain = getTableDomain(entityClass);
                    if (tableDomain != null) {// 如果没有在类名上声明Table注解则忽略该类
                        annotationSchemas.put(entityClass.getName(), tableDomain);
                    }
                }
            }
        }
        Cache.putAllTablesCache(annotationSchemas);
        return annotationSchemas;
    }

    private TableBean getTableDomain(Class<?> entityClass) {
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

    public List<String> getScanPackageList() {
        return scanPackageList;
    }

    public void setScanPackageList(List<String> scanPackageList) {
        this.scanPackageList = scanPackageList;
    }

    public Set<Class<?>> getEntityClasses() {
        return entityClasses;
    }

    public void setEntityClasses(Set<Class<?>> entityClasses) {
        this.entityClasses = entityClasses;
    }
}
