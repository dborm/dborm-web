package com.keqiaokeji.dborm.core;

import com.keqiaokeji.dborm.schema.DbormSchemaScan;
import com.keqiaokeji.dborm.util.StringUtilsDborm;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 根据实体解析出SQL
 * 
 * @author KEQIAO KEJI
 * @time 2013-5-3下午2:07:40
 */
public class ParseSql {

	private ParseEntity parseEntity;

    public ParseSql(){
        parseEntity = new ParseEntity();
    }

    /**
     * 解析出指定类的新增语句
     * @param entityClass 指定的类
     * @return 新增语句
     */
	public String getInsertSql(Class<?> entityClass) {
		// 例如： INSERT INTO users(user_Id, username) VALUES (?,?) ;
		String sql;
		StringBuilder sqlContent = new StringBuilder("INSERT INTO ");
		String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
		sqlContent.append(tableName);
		sqlContent.append(" (");
        StringBuilder columnName = new StringBuilder();
		StringBuilder columnValue = new StringBuilder();

		Map<String, Field> fields = parseEntity.getEntityColumnFields(entityClass);
		Set<Entry<String, Field>> entrySet = fields.entrySet();
		for (Entry<String, Field> entry : entrySet) {
			columnName.append(entry.getKey());
			columnName.append(", ");
			columnValue.append("?, ");
		}
		sqlContent.append(StringUtilsDborm.cutLastSign(columnName.toString(), ", "));
		sqlContent.append(") VALUES (");
		sqlContent.append(StringUtilsDborm.cutLastSign(columnValue.toString(), ", "));
		sqlContent.append(")");
		sql = sqlContent.toString();
		Cache.putSqlCache(entityClass.getName() + ".INSERT", sql);
		return sql;
	}

	public String getUpdateSql(Class<?> entityClass) {
		// 例如： UPDATE users SET user_id=?, user_name=?, user_age=? WHERE
		// user_id=?;
		String sql;
        StringBuilder sqlContent;
        sqlContent = new StringBuilder("UPDATE ");
        String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
		sqlContent.append(tableName);
		sqlContent.append(" SET ");
		StringBuilder columnName = new StringBuilder();

		Map<String, Field> columnFields = parseEntity.getEntityColumnFields(entityClass);
		Set<Entry<String, Field>> entrySet = columnFields.entrySet();
		for (Entry<String, Field> entry : entrySet) {
			columnName.append(entry.getKey());
			columnName.append("=?, ");
		}
		sqlContent.append(StringUtilsDborm.cutLastSign(columnName.toString(), ", "));
		sqlContent.append(" WHERE ");
		sqlContent.append(parsePrimaryKeyWhere(entityClass));
		sql = sqlContent.toString();
		Cache.putSqlCache(entityClass.getName() + ".UPDATE", sql);
		return sql;
	}

	public String getDeleteSql(Class<?> entityClass) {
		// 例如： DELETE FROM users WHERE user_id=?;
		String sql;
        StringBuilder sqlContent = new StringBuilder("DELETE FROM ");
		String tableName = DbormSchemaScan.getTableDomain(entityClass.getName()).getTableName();
		sqlContent.append(tableName);
		sqlContent.append(" WHERE ");
		sqlContent.append(parsePrimaryKeyWhere(entityClass));
		sql = sqlContent.toString();
		Cache.putSqlCache(entityClass.getName() + ".DELETE", sql);
		return sql;
	}

	/**
	 * 解析出where条件后面的主键信息
	 * 
	 * @param entityClass
	 *            实体类
	 * @return where条件后面的主键字符串
	 * @author KEQIAO KEJI
	 * @time 2013-5-4下午12:29:55
	 */
	public String parsePrimaryKeyWhere(Class<?> entityClass) {
		StringBuilder sqlContent = new StringBuilder();
		Map<String, Field> fields = parseEntity.getEntityPrimaryKeyFields(entityClass);
		Set<Entry<String, Field>> entrySet = fields.entrySet();
		for (Entry<String, Field> entry : entrySet) {
			sqlContent.append(entry.getKey());
			sqlContent.append("=? and ");

		}
		return StringUtilsDborm.cutLastSign(sqlContent.toString(), "and ");
	}

}
