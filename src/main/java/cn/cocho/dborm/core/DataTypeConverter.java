package cn.cocho.dborm.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 数据库的数据与Java数据的转换器
 *
 * @author KEQIAO KEJI
 * @time 2013-5-6下午12:01:06
 */
public class DataTypeConverter {

    /**
     * 将Java属性类型的值转换为数据列对应的值
     *
     * @param fieldValue Java类型的参数值
     * @return 数据列的值
     * @author KEQIAO KEJI
     * @time 2013-5-5上午2:36:18
     */
    public Object fieldValueToColumnValue(Object fieldValue) {
        if (fieldValue == null) {
            return null;
        }

        if (Date.class.equals(fieldValue.getClass())) {
            Date time = (Date) fieldValue;
            return time.getTime();
        }
        return fieldValue;
    }

    /**
     * 将数据列对应的值转换为Java属性类型的值
     *
     * @param rs         结果集
     * @param columnName 列名称
     * @param field      属性对象
     * @return 该属性类型的值
     * @throws SQLException
     * @author KEQIAO KEJI
     * @time 2013-5-5上午2:44:02
     */
    public Object columnValueToFieldValue(ResultSet rs, String columnName, Field field) throws SQLException {
        if (Date.class.equals(field.getType())) {
            long time = rs.getLong(columnName);
            return new Date(time);
        }
        return rs.getObject(columnName);
    }

}
