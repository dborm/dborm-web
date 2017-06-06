package com.tbc.paas.mql.metadata;

import java.util.Map;

import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.Table;
import com.tbc.paas.mql.metadata.domain.TableRelation;
import com.tbc.paas.mql.metadata.domain.TableView;

public interface MqlMetadataService {
	/**
	 * 获取表的基本信息,包括表名，表主键,列的基本信息,不包括可扩展列
	 * 
	 * @param appCode
	 *            应用编码
	 * @param corpCode
	 *            公司编码
	 * @param TableName
	 *            所要查看表信息的表名
	 * @return 表的基本信息.
	 */
	Table getTable(String appCode, String corpCode, String tableName);

	/**
	 * 获取可扩展表对应的虚拟列的信息
	 * 
	 * @param appCode
	 *            应用编码
	 * @param corpCode
	 *            公司编码
	 * @param tableName
	 *            所要查看表信息的表名
	 * @return 表的拓展信息.
	 */
	Map<String, CorpTable> getCorpTable(String appCode, String corpCode,
			String tableName);

	/**
	 * 获取表的基本信息和拓展信息
	 * 
	 * @param appCode
	 *            应用编码
	 * @param corpCode
	 *            公司编码
	 * @param tableName
	 *            所要查看表信息的表名
	 * 
	 * @return 表的基本信息和拓展信息.
	 */
	TableView getTableView(String appCode, String corpCode, String tableName);

	/**
	 * 这个方法用于提供表和表之间的关联关.
	 * 
	 ** @param appCode
	 *            应用编码
	 * @param corpCode
	 *            公司编码
	 * @param fromTableName
	 *            主表的名称.
	 * @param toTableName
	 *            从表的名称.
	 * @return 主表和从表之间的关系描述.如果主表和从表之间没有配置任何关联关系,则返回空.
	 */
	TableRelation getTableRelation(String appCode, String corpCode,
			String fromTableName, String toTableName);
}
