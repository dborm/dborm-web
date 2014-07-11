package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.domain.ColumnBean;
import com.keqiaokeji.dborm.schema.SchemaConstants;
import com.keqiaokeji.dborm.util.ReflectUtilsDborm;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * 解析实体
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午11:28:20
 */
public class ParseEntity {

    private ConvertDBType convertType;

    public ParseEntity() {
        convertType = new ConvertDBType();
    }

    /**
     * 获得实体类的全部属性
     *
     * @param entityClass 实体类
     * @return 全部属性集（键：列名，值：属性对象）
     * @author KEQIAO KEJI
     * @time 2013-5-10下午2:01:26
     */
    public Map<String, Field> getEntityAllFields(Class<?> entityClass) {
        String entityName = entityClass.getName();
        Map<String, Field> allFields = Cache.getEntityAllFieldsCache(entityName);
        if (allFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            List<Field> fields = ReflectUtilsDborm.getFields(entityClass);
            allFields = new HashMap<String, Field>();
            for (Field field : fields) {
                allFields.put(StringUtilsDborm.generateUnderlineName(field.getName()), field);
            }
            Cache.putEntityAllFieldsCache(entityName, allFields);
        }
        return allFields;
    }

    /**
     * 获得实体类的列属性（属性上有column标注的属性）
     *
     * @param entityClass 实体类
     * @return 列属性集（键：列名，值：属性对象）
     * @author KEQIAO KEJI
     * @time 2013-5-8上午11:03:02
     */
    public Map<String, Field> getEntityColumnFields(Class<?> entityClass) {
        Map<String, Field> columnFields = Cache.getEntityColumnFieldsCache(entityClass.getName());
        if (columnFields == null) {// 如果缓存中不存在该对象的反射信息则需解析
            columnFields = new HashMap<String, Field>();
            Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
            Map<String, Field> allFields = getEntityAllFields(entityClass);
            for (Entry<String, Field> entry : allFields.entrySet()) {
                Field field = entry.getValue();
                ColumnBean column = columns.get(entry.getKey());
                if (column == null) {// 如果没有column标注
                    continue;
                }
                String columnName = StringUtilsDborm.generateUnderlineName(field.getName());
                columnFields.put(columnName, field);
            }
            Cache.putEntityColumnFieldsCache(entityClass.getName(), columnFields);
        }
        return columnFields;
    }

    /**
     * 获得实体类的主键属性（属性上有PrimaryKey标注的属性）
     *
     * @param entityClass 实体类
     * @return 列属性集（键：列名，值：属性对象）
     * @author KEQIAO KEJI
     * @time 2013-5-8上午11:03:02
     */
    public Map<String, Field> getEntityPrimaryKeyFields(Class<?> entityClass) {
        String entityName = entityClass.getName();
        Map<String, Field> primaryKeys = Cache.getEntityPrimaryKeyFieldsCache(entityName);
        if (primaryKeys == null) {// 如果缓存中不存在该对象的反射信息则需解析
            primaryKeys = new HashMap<String, Field>();
            Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
            Set<Entry<String, ColumnBean>> entrySet = columns.entrySet();
            for (Entry<String, ColumnBean> entry : entrySet) {
                ColumnBean column = entry.getValue();
                if (column.isPrimaryKey()) {
                    Field field = ReflectUtilsDborm.getFieldByName(entityClass, column.getFieldName());
                    String columnName = StringUtilsDborm.generateUnderlineName(field.getName());
                    primaryKeys.put(columnName, field);
                }
            }
            Cache.putEntityPrimaryKeyFieldsCache(entityName, primaryKeys);
        }
        return primaryKeys;
    }

    /**
     * 获得指定实体的列属性值集合
     *
     * @param entity 实体
     * @param <T>    实体类型
     * @return 实体column属性值集合
     * @author KEQIAO KEJI
     * @time 2013-5-3上午11:26:28
     */
    public <T> List<Object> getColumnFiledValues(T entity) {
        Class<?> entityClass = entity.getClass();
        List<Object> fieldValues = new ArrayList<Object>();
        Map<String, Field> columnFields = getEntityColumnFields(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            value = convertType.convertToColumnValue(value);
            fieldValues.add(value);
        }
        return fieldValues;
    }

    /**
     * 获得指定实体的列属性值集合
     *
     * @param entity 实体
     * @param <T>    实体类型
     * @return 实体column属性值集合
     * @author KEQIAO KEJI
     * @time 2013-5-3上午11:26:28
     */
    public <T> List<Object> getColumnFiledValuesUseDefault(T entity) {
        Class<?> entityClass = entity.getClass();
        List<Object> fieldValues = new ArrayList<Object>();
        Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
        Map<String, Field> columnFields = getEntityColumnFields(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            if (value == null) {//如果属性的值为空，则查看一下该属性是否设置的有默认值，如果默认值不为空则使用默认值
                Object defaultValue = columns.get(entry.getKey()).getDefaultValue();
                if (defaultValue != null && !defaultValue.toString().equalsIgnoreCase(SchemaConstants.DEFAULT_VALUE_NULL)) {
                    value = defaultValue;
                }
            }
            value = convertType.convertToColumnValue(value);
            fieldValues.add(value);
        }
        return fieldValues;
    }

    /**
     * 获得指定实体的主键属性值集合
     *
     * @param entity 实体
     * @param <T>    实体类型
     * @return 实体主键属性值集合
     * @author KEQIAO KEJI
     * @time 2013-5-3上午11:26:28
     */
    public <T> List<Object> getPrimaryKeyFiledValues(T entity) {
        List<Object> primaryKeyValues = new ArrayList<Object>();
        Class<?> entityClass = entity.getClass();
        Map<String, Field> primaryKeyFields = getEntityPrimaryKeyFields(entityClass);
        Set<Entry<String, Field>> entrySet = primaryKeyFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            if (value != null) {
                value = convertType.convertToColumnValue(value);
                primaryKeyValues.add(value);
            } else {
                throw new IllegalArgumentException(" The primary key(" + field.getName() + ") in class(" + entityClass.getName()
                        + ")  Can't be empty!");
            }
        }
        return primaryKeyValues;
    }

    /**
     * 判断实体的主键是否为空(使用联合主键时，任意一个主键为空，则判断结果为空)
     *
     * @param entity 实体对象
     * @return true：空，false：非空
     * @author KEQIAO KEJI
     * @time 2013-5-6下午2:25:59
     */
    public <T> boolean primaryKeyIsNull(T entity) {
        boolean result = false;
        Class<?> entityClass = entity.getClass();
        Map<String, Field> paimaryKeyFields = getEntityPrimaryKeyFields(entityClass);
        Set<Entry<String, Field>> entrySet = paimaryKeyFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            if (value == null) {
                result = true;
            }
        }
        return result;
    }




}
