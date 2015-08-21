package cn.cocho.dborm.core;

import cn.cocho.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * SQL转换器（根据实体转换出相应的SQL）
 *
 * @author KEQIAO KEJI
 * @time 2013-5-3下午2:07:40
 */
public class SQLTranslater {

    StringUtilsDborm stringUtils = new StringUtilsDborm();

    /**
     * 解析出实体类的新增SQL语句
     *
     * @param entityClass 实体类
     * @return 新增SQL语句
     */
    public String getInsertSql(Class<?> entityClass) {
        // 例如： INSERT INTO users(user_Id, username) VALUES (?,?) ;
        String sql;
        StringBuilder sqlContent = new StringBuilder("INSERT INTO ");
        String tableName = CacheDborm.getCache().getTablesCache(entityClass).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" (");
        StringBuilder columnName = new StringBuilder();
        StringBuilder columnValue = new StringBuilder();

        Map<String, Field> fields = CacheDborm.getCache().getEntityColumnFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = fields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            columnName.append(entry.getKey());
            columnName.append(", ");
            columnValue.append("?, ");
        }
        sqlContent.append(stringUtils.cutLastSign(columnName.toString(), ", "));
        sqlContent.append(") VALUES (");
        sqlContent.append(stringUtils.cutLastSign(columnValue.toString(), ", "));
        sqlContent.append(")");
        sql = sqlContent.toString();

        return sql;
    }

    /**
     * 解析出实体类的删除SQL语句
     *
     * @param entityClass 实体类
     * @return 删除SQL语句
     */
    public String getDeleteSql(Class<?> entityClass) {
        // 例如： DELETE FROM users WHERE user_id=?;
        String sql;
        StringBuilder sqlContent = new StringBuilder("DELETE FROM ");
        String tableName = CacheDborm.getCache().getTablesCache(entityClass).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" WHERE ");
        sqlContent.append(parsePrimaryKeyWhere(entityClass));
        sql = sqlContent.toString();
        return sql;
    }

    /**
     * 解析出实体类的修改SQL语句
     *
     * @param entityClass 实体类
     * @return 修改SQL语句
     */
    public String getUpdateSql(Class<?> entityClass) {
        // 例如： UPDATE users SET user_id=?, user_name=?, user_age=? WHERE user_id=?;
        String sql;
        StringBuilder sqlContent;
        sqlContent = new StringBuilder("UPDATE ");
        String tableName = CacheDborm.getCache().getTablesCache(entityClass).getTableName();
        sqlContent.append(tableName);
        sqlContent.append(" SET ");
        StringBuilder columnName = new StringBuilder();

        Map<String, Field> columnFields = CacheDborm.getCache().getEntityColumnFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = columnFields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            columnName.append(entry.getKey());
            columnName.append("=?, ");
        }
        sqlContent.append(stringUtils.cutLastSign(columnName.toString(), ", "));
        sqlContent.append(" WHERE ");
        sqlContent.append(parsePrimaryKeyWhere(entityClass));
        sql = sqlContent.toString();
        return sql;
    }

    /**
     * 解析出where条件后面的主键SQL语句
     *
     * @param entityClass 实体类
     * @return where条件后面的主键SQL语句
     */
    public String parsePrimaryKeyWhere(Class<?> entityClass) {
        StringBuilder sqlContent = new StringBuilder();
        Map<String, Field> fields = CacheDborm.getCache().getEntityPrimaryKeyFieldsCache(entityClass);
        Set<Entry<String, Field>> entrySet = fields.entrySet();
        for (Entry<String, Field> entry : entrySet) {
            sqlContent.append(entry.getKey());
            sqlContent.append("=? and ");

        }
        return stringUtils.cutLastSign(sqlContent.toString(), "and ");
    }

}
