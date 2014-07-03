package com.keqiaokeji.dborm.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Dborm中Java与数据库之间的类型转换
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6下午12:01:06
 */
class ConvertDBType {

    /**
     * 一定不能支持基本数值类型，如支持Integer但是不能支持int，原因如下：<br>
     * 1.replace的原则是忽略null字段<br>
     * 2.数据库取出的值未必会有值，没有值的时候应该对属性的初始值赋值为null
     */
    private static final String SUPPORT_TYPE = "only supported[String, Integer, Boolean, Date, Long, Float, Double, Short, Byte]";


    /**
     * 将Java类型的值转换为数据列对应的值
     *
     * @param paramValue Java类型的参数值
     * @return 数据列的值
     * @author KEQIAO KEJI
     * @time 2013-5-5上午2:36:18
     */
    public Object convertToColumnValue(Object paramValue) {
        if (paramValue == null) {
            return null;
        }

        Class<?> type = paramValue.getClass();
        if (isBoolean(type)) {
            boolean value = Boolean.parseBoolean(paramValue.toString());
            return value ? 1 : 0;
        } else if (isDate(type)) {
            Date time = (Date) paramValue;
            return time.getTime();
        } else if (isByte(Byte.class)) {
            return paramValue;
        }
        return paramValue;
    }

    /**
     * 将数据列对应的值转换为Java类型的值
     *
     * @param rs          结果集
     * @param columnIndex 列的编号（从1开始）
     * @param field       属性对象
     * @return 该属性类型的值
     * @throws SQLException
     * @author KEQIAO KEJI
     * @time 2013-5-5上午2:44:02
     */
    public Object convertToParamValue(ResultSet rs, int columnIndex, Field field) throws SQLException {
        Class<?> type = field.getType();
        // Object obj = rs.getObject(columnIndex, type);
        if (rs.getObject(columnIndex) == null) {
            return null;
        }

        try {
            if (isString(type)) {
                return rs.getString(columnIndex);
            } else if (isInteger(type)) {
                return rs.getInt(columnIndex);
            } else if (isBoolean(type)) {
                return rs.getShort(columnIndex) > 0;
            } else if (isDate(type)) {
                long time = rs.getLong(columnIndex);
                return new Date(time);
            } else if (isLong(type)) {
                return rs.getLong(columnIndex);
            } else if (isFloat(type)) {
                return rs.getFloat(columnIndex);
            } else if (isDouble(type)) {
                return rs.getDouble(columnIndex);
            } else if (isShort(type)) {
                return rs.getShort(columnIndex);
            } else if (isByte(type)) {
                return rs.getBlob(columnIndex);
            } else {
                throw new UnsupportedOperationException("the attribute tableName[" + field.getName() + "] declare in class["
                        + field.getDeclaringClass() + "] use type[" + type.getName() + "] have not been added to supported!\n"
                        + SUPPORT_TYPE);
            }
        } catch (Exception e) {
            throw new RuntimeException("convert column index[" + columnIndex + "] value to attribute tableName[" + field.getName()
                    + "] value has error!", e);
        }

    }

    private static boolean isString(Class<?> type) {
        return String.class.equals(type);
    }

    private static boolean isInteger(Class<?> type) {
        return Integer.class.equals(type);
    }

    private static boolean isBoolean(Class<?> type) {
        return Boolean.class.equals(type);
    }

    private static boolean isFloat(Class<?> type) {
        return Float.class.equals(type);
    }

    private static boolean isLong(Class<?> type) {
        return Long.class.equals(type);
    }

    private static boolean isDouble(Class<?> type) {
        return Double.class.equals(type);
    }

    private static boolean isDate(Class<?> type) {
        return Date.class.equals(type);
    }

    private static boolean isShort(Class<?> type) {
        return Short.class.equals(type);
    }

    private static boolean isByte(Class<?> type) {
        return type.isArray() && (type.getComponentType().equals(byte.class) || type.getComponentType().equals(Byte.class));
    }

}
