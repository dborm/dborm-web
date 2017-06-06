package com.tbc.paas.mql.parser.dialect;

import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.metadata.domain.Column;
import com.tbc.paas.mql.parser.MqlParser;
import com.tbc.paas.mql.parser.MqlValueParser;
import com.tbc.paas.mql.util.SqlBuilder;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Clob;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 这个类是Oracle的方言库,用于处理Oracle的方言.
 * 
 * @author Ztian
 * 
 */
public class OracleDialect {
	public static final Logger LOG = Logger.getLogger(OracleDialect.class);

	/**
	 * 这个用于拼接Oracle的分页查询语句.
	 * 
	 * @param sqlBuilder
	 *            实际的查询语句.
	 * @param limit
	 *            分页返回数量.
	 * @param offset
	 *            相对于第一行的偏移量.
	 * @return 最终的SqlBuilder.
	 */
	private SqlBuilder getLimitSqlBuilder(SqlBuilder sqlBuilder, Integer limit,
			Integer offset) {

		SqlBuilder pagingSelect = new SqlBuilder();
		if (offset >= 0) {
			pagingSelect
					.append("select * from ( select row_.*, rownum rownum_ from ( ")
					.append(sqlBuilder)
					.append(" ) row_ ) where rownum_ > ? and rownum_ <= ?")
					.addParameter(offset).addParameter(offset + limit);
		} else {
			pagingSelect.append("select * from ( ").append(sqlBuilder)
					.append(" ) where rownum <= ?").addParameter(limit);
		}

		return pagingSelect;
	}

	/**
	 * 这个用于处理分页部分的Sql.
	 * 
	 * @param sqlBuilder
	 *            实际的查询语句.
	 * @param limitSegment
	 *            分页查询的SqlNode.
	 * @param parameterList
	 *            以问号形式传递的参数.
	 * @param parameterMap
	 *            以占位符形式传递的参数.
	 * @return 最终的查询语句.
	 */
	public SqlBuilder processLimitSegment(SqlBuilder sqlBuilder,
			SqlNode limitSegment, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		SqlBuilder limitSegmentSql = new SqlBuilder();
		int childrenCount = limitSegment.getChildrenCount();
		limitSegment.dump("");

		Integer limitValue = -1;
		Integer offsetValue = -1;

		for (int i = 0; i < childrenCount; i++) {
			SqlNode childNode = limitSegment.getChild(i);
			int childId = childNode.getId();
			if (childId == SqlGrammarTreeConstants.JJTLIMIT) {
				SqlNode limitValueNode = childNode.getChild(0);
				MqlValueParser valueNodeParser = new MqlValueParser(
						limitSegmentSql, parameterList, parameterMap);
				valueNodeParser.processValuePositionNode(limitValueNode);
				limitValue = (Integer) valueNodeParser.getValue();
			} else if (childId == SqlGrammarTreeConstants.JJTOFFSET) {
				SqlNode offsetValueNode = childNode.getChild(0);
				MqlValueParser valueNodeParser = new MqlValueParser(
						limitSegmentSql, parameterList, parameterMap);
				valueNodeParser.processValuePositionNode(offsetValueNode);
				offsetValue = (Integer) valueNodeParser.getValue();
			}
		}

		return getLimitSqlBuilder(sqlBuilder, limitValue, offsetValue);
	}

	/**
	 * oracle方言对于数据类型的转换（DTO字段转数据库字段）
	 * 
	 * @author ZHANG Nan
	 * @param columnField
	 *            字段
	 * @return 数据库对应的数据类型
	 */
	public static String getSqlTypeByField(Field columnField) {
		Class<?> filedType = columnField.getType();
		if (filedType.equals(Integer.class) || filedType.equals(int.class)) {
			return "integer";
		}
		if (filedType.equals(Long.class) || filedType.equals(long.class)) {
			return "integer";
		}
		if (filedType.equals(Float.class) || filedType.equals(float.class)) {
			return "float";
		}
		if (filedType.equals(BigDecimal.class)) {
			return "decimal";
		}
		if (filedType.equals(Date.class)) {
			return "date";
		}
		if (filedType.equals(String.class)) {
			return "varchar2";
		}
		if (filedType.equals(Boolean.class) || filedType.equals(boolean.class)) {
			return "number";
		}
		if (filedType.equals(Double.class) || filedType.equals(double.class)) {
			return "double";
		}
		return filedType.toString();
	}

	/**
	 * oracle方言对于数据类型的转换（数据库字段转DTO字段）
	 * 
	 * @author ZHANG Nan
	 * @param columnField
	 *            字段
	 * @param data
	 *            值
	 * @return 转换后的值
	 */
	public static Object dataTypeDialectConvert(Field columnField, Object data) {
		// 如果oracle数据库标记未开启则直接返回
		if (!MqlParser.isOracle()) {
			return data;
		}

		// 对字段类型为bool进行方言转换
		if (columnField.getType().equals(Boolean.class)) {
			if (data != null && "1".equals(data.toString())) {
				return true;
			} else {
				return false;
			}
		}
		if (columnField.getType().equals(Integer.class)
				&& data instanceof BigDecimal) {
			return ((BigDecimal) data).intValue();
		}
		// 对字段类型为Long进行方言转换
		if (columnField.getType().equals(Long.class)
				&& data instanceof BigDecimal) {
			return ((BigDecimal) data).longValue();
		}
		// 对字段类型为Float进行方言转换
		if (columnField.getType().equals(Float.class)
				&& data instanceof BigDecimal) {
			return ((BigDecimal) data).floatValue();
		}
		// 对字段类型为Double进行方言转换
		if (columnField.getType().equals(Double.class)
				&& data instanceof BigDecimal) {
			return ((BigDecimal) data).doubleValue();
		}
		// 对字段类型为clob进行方言转换
		if (columnField.getType().equals(String.class) && data instanceof Clob) {
			Clob clobData = (Clob) data;
			try {
				InputStream inputStream = clobData.getAsciiStream();
				byte[] b = new byte[4096];
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int i = -1;
				while ((i = inputStream.read(b)) != -1) {
					byteArrayOutputStream.write(b, 0, i);
				}
				return new String(byteArrayOutputStream.toByteArray(), "UTF-8");

			} catch (Exception e) {
				LOG.error("com.tbc.paas.mql.parser.dialect.OracleDialect.dataTypeDialectConvert(Field, Object):"
						+ e);
			}
		}

		return data;
	}

	/**
	 * oracle方言对于数据类型的转换（数据库字段转DTO字段）（对于聚合函数方言转换）
	 * 
	 * @author ZHANG Nan
	 *            字段
	 * @param data
	 *            值
	 * @return 转换后的值
	 */
	public static Object aggregationDataTypeDialectForUnencop(
			SqlResultColumn sqlResultColumn, Object data,
			EntityMaping entityMaping) {

		if (data == null) {
			return data;
		}

		// 如果oracle数据库标记未开启则直接返回
		if (!MqlParser.isOracle()) {
			return data;
		}

		// 是否为聚合函数
		if (!sqlResultColumn.isAggregation()) {
			return data;
		}

		// 是否是求平均值
		String columnName = sqlResultColumn.getColumnName();
		if ("avg".equalsIgnoreCase(columnName)) {
			// 返回double类型
			return (((BigDecimal) data).doubleValue());
		} else if ("sum".equalsIgnoreCase(columnName)
				|| "max".equalsIgnoreCase(columnName)
				|| "min".equalsIgnoreCase(columnName)) {
			// 获取数据库列名
			String columnAlias = sqlResultColumn.getColumnAlias();
			if (columnAlias != null && columnAlias.indexOf(".") >= 0) {
				// 去除别名
				columnAlias = columnAlias.split("\\.")[1];
			}
			Column column = entityMaping.getColumnMap().get(columnAlias);
			if (column != null && "float8".equalsIgnoreCase(column.getColumnType())) {
				return (((BigDecimal) data).doubleValue());
			} 
			else if (column != null && "float4".equalsIgnoreCase(column.getColumnType())) {
				return (((BigDecimal) data).floatValue());
			}
			//如果为int8直接返回原数据类型
			else if (column !=null && "int8".equalsIgnoreCase(column.getColumnType())){
				return data;
			}
            //如果为timestamp直接返回原数据类型
            else if (column !=null && "timestamp".equalsIgnoreCase(column.getColumnType())){
				return data;
			}

			return (((BigDecimal) data).longValue());
		} else {
			// 返回long类型
			return (((BigDecimal) data).longValue());
		}
	}

	public static Object rowDataTypeDialectForUnencop(
			SqlResultColumn sqlResultColumn, Object data,
			EntityMaping entityMaping) {
		if (data == null) {
			return data;
		}

		// 如果oracle数据库标记未开启则直接返回
		if (!MqlParser.isOracle()) {
			return data;
		}

		String fieldName = sqlResultColumn.getColumnName();
		Column column = entityMaping.getColumnByFieldName(fieldName);
		String columnType = column.getColumnType();

		// 对字段类型为bool进行方言转换
		if (columnType.equalsIgnoreCase("boolean")) {
			if (!"0".equals(data.toString())) {
				return true;
			} else {
				return false;
			}
		}

		if (data instanceof BigDecimal) {
			if ("float8".equalsIgnoreCase(columnType)) {
				return (((BigDecimal) data).doubleValue());
			} else if ("float4".equalsIgnoreCase(columnType)) {
				return (((BigDecimal) data).floatValue());
			} else if ("long".equalsIgnoreCase(columnType)) {
				return (((BigDecimal) data).longValue());
			} else {
				return (((BigDecimal) data).intValue());
			}
		}

		// 对字段类型为clob进行方言转换
		if (data instanceof Clob) {
			Clob clobData = (Clob) data;
			try {
				InputStream inputStream = clobData.getAsciiStream();
				byte[] b = new byte[4096];
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int i = -1;
				while ((i = inputStream.read(b)) != -1) {
					byteArrayOutputStream.write(b, 0, i);
				}
				return new String(byteArrayOutputStream.toByteArray(), "UTF-8");

			} catch (Exception e) {
				LOG.error("com.tbc.paas.mql.parser.dialect.OracleDialect.dataTypeDialectConvert(Field, Object):"
						+ e);
			}
		}

		return data;
	}
}
