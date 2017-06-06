package com.tbc.paas.mql.metadata;

import java.util.List;
import java.util.Map;

/**
 * 这个接口用于存储各个应用中表的大字段.
 * 
 * @author Ztian
 * 
 */
public interface MqlBigTextService {

	/**
	 * 查询某个应用某个表名称下某行某列的值.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowId
	 *            对应列的主键Id
	 * @param columnName
	 *            获取数据的列名.
	 * @return 对应列的值,如果值不存在,则返回null.
	 */
	String queryBigTextColumn(String appCode, String tableName, String rowId,
			String columnName);

	/**
	 * 查询某个应用某个表下多行某列的所有值.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowIds
	 *            对应行的Id列表.
	 * @param columnName
	 *            获取数据的列名.
	 * @return 对应列的值,如果值不存在,则该行返回null.
	 */
	List<String> queryBigTextColumnList(String appCode, String tableName,
			List<String> rowIds, String columnName);

	/**
	 * 查询某个应用某个表下多行多列的所有大字段值. <br>
	 * 返回的结果列表中,每行的数据存储在一个Map中.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowIds
	 *            行的Id列表.
	 * @param columnNames
	 *            要获取的列明列表.
	 * @return 返回的结果列表中,每行的数据存储在一个Map中.如果整列没有数据,这Map中没有值.
	 */
	List<Map<String, String>> queryBigTextColumns(String appCode,
			String tableName, List<String> rowIds, List<String> columnNames);

	/**
	 * 保存或者更新某个应用某个表的的某个大字段列的类容.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowId
	 *            对应列的主键Id
	 * @param columnName
	 *            获取数据的列名.
	 * @param value
	 *            需要保存的值.
	 */
	void saveBigTextColumn(String appCode, String tableName, String rowId,
			String columnName, String value);

	/**
	 * 
	 * 保存或者更新某个应用某个表的的某个大字段列的类容.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowId
	 *            对应列的主键Id
	 * @param columnMap
	 *            需要保存的列及对应的值组成的Map.
	 */
	void saveBigTextColumn(String appCode, String tableName, String rowId,
			Map<String, String> columnMap);

	/**
	 * 删除某个应用某个表的某列大字段的记录.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowId
	 *            对应列的主键Id
	 */
	void deleteBigTextColumn(String appCode, String tableName, String rowId);

	/**
	 * 删除某个应用某个表的多行大字段的记录.
	 * 
	 * @param appCode
	 *            应用编码
	 * @param tableName
	 *            表名称
	 * @param rowIds
	 *            删除行的主键Id列表.
	 */
	void deleteBigTextColumn(String appCode, String tableName,
			List<String> rowIds);
}
