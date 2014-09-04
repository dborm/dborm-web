package cn.cocho.dborm.core;

import cn.cocho.dborm.domain.ColumnBean;
import cn.cocho.dborm.schema.SchemaConstants;
import cn.cocho.dborm.util.ReflectUtilsDborm;
import cn.cocho.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 实体解析器
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6上午11:28:20
 */
public class EntityResolver {

    /**
     * 获得实体类的全部属性
     *
     * @param entityClass 实体类
     * @return 全部属性集（键：列名，值：属性对象）
     * @author KEQIAO KEJI
     * @time 2013-5-10下午2:01:26
     */
    public static Map<String, Field> getEntityAllFields(Class<?> entityClass) {
        List<Field> fields = ReflectUtilsDborm.getFields(entityClass);
        Map<String, Field> allFields = new HashMap<String, Field>();
        for (Field field : fields) {
            allFields.put(StringUtilsDborm.humpToUnderlineName(field.getName()), field);
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
    public static Map<String, Field> getEntityColumnFields(Class<?> entityClass) {
        Map<String, Field> columnFields = new HashMap<String, Field>();
        Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
        Map<String, Field> allFields = Cache.getEntityAllFieldsCache(entityClass);
        for (Entry<String, Field> fieldInfo : allFields.entrySet()) {
            Field field = fieldInfo.getValue();
            if (columns.containsKey(fieldInfo.getKey())) {// 如果表的列属性信息中包含该属性，则说明该属性属为列属性
                columnFields.put(fieldInfo.getKey(), field);
            }
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
    public static Map<String, Field> getEntityPrimaryKeyFields(Class<?> entityClass) {
        Map<String, Field> primaryKeys = new HashMap<String, Field>();
        Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
        for (Entry<String, ColumnBean> entry : columns.entrySet()) {
            ColumnBean column = entry.getValue();
            if (column.isPrimaryKey()) {
                Field field = ReflectUtilsDborm.getFieldByName(entityClass, column.getFieldName());
                primaryKeys.put(entry.getKey(), field);
            }
        }
        return primaryKeys;
    }

    /**
     * 将结果集转换为实体对象
     *
     * @param entityClass 实体类
     * @param rs          结果集
     * @param columnNames 结果集中包含的列名
     * @return 实体对象
     * @throws SQLException
     */
    public static Object getEntitys(Class<?> entityClass, ResultSet rs, String[] columnNames) throws SQLException {
        Map<String, Field> fields = Cache.getEntityAllFieldsCache(entityClass);// 获得该类的所有属性，支持联合查询
        Object entity = ReflectUtilsDborm.createInstance(entityClass);// 创建实体类的实例
        for (int i = 0; i < columnNames.length; i++) {
            Field field = fields.get(columnNames[i]);
            if (field != null) {
                Object value = DataTypeConverter.columnValueToFieldValue(rs, columnNames[i], field);
                ReflectUtilsDborm.setFieldValue(field, entity, value);
            }
        }
        return entity;
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
    public static <T> List<Object> getColumnFiledValues(T entity) {
        Class<?> entityClass = entity.getClass();
        List<Object> fieldValues = new ArrayList<Object>();
        Map<String, Field> columnFields = Cache.getEntityColumnFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            value = DataTypeConverter.fieldValueToColumnValue(value);
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
    public static <T> List<Object> getColumnFiledValuesUseDefault(T entity) {
        Class<?> entityClass = entity.getClass();
        List<Object> fieldValues = new ArrayList<Object>();
        Map<String, ColumnBean> columns = Cache.getTablesCache(entityClass).getColumns();
        Map<String, Field> columnFields = Cache.getEntityColumnFieldsCache(entityClass);
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
            value = DataTypeConverter.fieldValueToColumnValue(value);
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
    public static <T> List<Object> getPrimaryKeyFiledValues(T entity) {
        List<Object> primaryKeyValues = new ArrayList<Object>();
        Class<?> entityClass = entity.getClass();
        Map<String, Field> primaryKeyFields = Cache.getEntityPrimaryKeyFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = primaryKeyFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            Field field = entry.getValue();
            Object value = ReflectUtilsDborm.getFieldValue(field, entity);
            if (value != null) {
                value = DataTypeConverter.fieldValueToColumnValue(value);
                primaryKeyValues.add(value);
            } else {
                throw new IllegalArgumentException(" 属性(" + field.getName() + ") 在类(" + entityClass.getName() + ")里面是主键，不能为空!");
            }
        }
        return primaryKeyValues;
    }


}
