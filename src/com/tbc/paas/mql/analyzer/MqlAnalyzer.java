package com.tbc.paas.mql.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlPhase;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.metadata.MqlMetadataService;
import com.tbc.paas.mql.metadata.domain.Column;
import com.tbc.paas.mql.metadata.domain.CorpColumn;
import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.Table;
import com.tbc.paas.mql.metadata.domain.TableRelation;
import com.tbc.paas.mql.metadata.domain.TableView;
import com.tbc.paas.mql.util.SqlAliasGenerator;
import com.tbc.paas.mql.util.SqlConstants;

/**
 * 这个类并不实际承担任何工作,但是完成了的Sql转换中大部分的预处理工作,<br>
 * 为实际Sql的转换做准备. 是的核心类之一.
 * 
 * @author Ztian
 * 
 */
public abstract class MqlAnalyzer {

	public static final Logger LOG = Logger.getLogger(MqlAnalyzer.class);

	/**
	 * 下面的几个字段，出了别名生成器有默认值以外， 其余变量(appCode,corpCode,<br>
	 * sqlMetadata,metadataService ) 都必须被使用的程序设置。
	 */
	protected String appCode;
	protected String corpCode;
	protected SqlAliasGenerator aliasGenerator;
	protected MqlMetadataService metadataService;

	private boolean careRelation;
	// 这个变量用于记录当前sql语句中所有涉及的表及相关表的元数据信息。
	protected Map<String, TableView> tableMetadataViewMap;
	// 这个字段主要存储SqlColumn和SqlResultColumn的对应关系.
	protected Map<SqlColumn, SqlResultColumn> sqlColumnResultMap;
	// 这个字段主要记录返回列表中标和列的对应关系.
	protected Map<SqlTable, List<SqlColumn>> sqlTableColumnMap;
	// 这两个字段主要存储SqlColumn和SqlTable的对应关系.
	protected Map<SqlColumn, SqlTable> sqlColumnTableMap;
	// 这个字段主要存储SqlColumn和主表的非拓展列的对应关系.
	protected Map<SqlColumn, Column> sqlColumnColumnMap;
	// 这个字段主要存储SqlColumn和拓展表的拓展列的对应关系.
	protected Map<SqlColumn, CorpColumn> sqlColumnCorpColumnMap;
	// 这个字段主要缓存主表和他的拓展表的对应关系.
	protected Map<SqlTable, Map<String, SqlTable>> tableAndExtTableMap;
	// 这个字段主要用于保存别名点星的所有字段a.*
	protected Map<SqlColumn, List<SqlColumn>> sqlAsteriskColumnMap;
	// 这个字段用于存储SqlTable之间的关系列
	protected List<SqlColumn> sqlTableRelColumnList;

	public MqlAnalyzer() {
		this(null, null, null);
	}

	public MqlAnalyzer(String appCode, String corpCode) {
		this(appCode, corpCode, null);
	}

	public MqlAnalyzer(String appCode, String corpCode,
			MqlMetadataService metadataService) {
		this.appCode = appCode;
		this.corpCode = corpCode;
		this.careRelation = true;
		this.metadataService = metadataService;
	}

	/**
	 * 分析传入的Metadata信息.
	 */
	public void analyze() {
		init();
		Map<String, SqlTable> usedTableNameSqlTableMap = analyzeSqlTable();
		analyzeSqlColumns(usedTableNameSqlTableMap);
		if (careRelation) {
			analyzeSqlTableRelation(usedTableNameSqlTableMap);
		}
	}

	/**
	 * 初始化数据所有的内部变量.
	 */
	private void init() {
		aliasGenerator = new SqlAliasGenerator();
		sqlTableColumnMap = new HashMap<SqlTable, List<SqlColumn>>();
		sqlTableRelColumnList = new ArrayList<SqlColumn>();
		tableMetadataViewMap = new HashMap<String, TableView>();
		sqlColumnTableMap = new HashMap<SqlColumn, SqlTable>();
		sqlColumnColumnMap = new HashMap<SqlColumn, Column>();
		sqlColumnResultMap = new HashMap<SqlColumn, SqlResultColumn>();
		sqlAsteriskColumnMap = new HashMap<SqlColumn, List<SqlColumn>>();
		sqlColumnCorpColumnMap = new HashMap<SqlColumn, CorpColumn>();
		tableAndExtTableMap = new HashMap<SqlTable, Map<String, SqlTable>>();
	}

	/**
	 * 分析表的的元数据信息
	 */
	private Map<String, SqlTable> analyzeSqlTable() {
		// 这个列表用于记录用出现过的最终表名称,任何一个最终的表名,最终只能出现一次.
		Map<String, SqlTable> tableUsedNameMap = new HashMap<String, SqlTable>();
		List<SqlTable> sqlTableList = getSqlTableList();
		for (SqlTable sqlTable : sqlTableList) {
			String tableName = sqlTable.getTableName();
			String tableAlias = sqlTable.getTableAlias();

			String tableUsedName = tableAlias;
			if (tableAlias == null || tableAlias.isEmpty()) {
				tableUsedName = tableName;
				tableAlias = aliasGenerator.generate(tableName);
				sqlTable.setTableAlias(tableAlias);
			}

			if (tableUsedNameMap.containsKey(tableUsedName)) {
				throw new MqlParseException("Table name[" + tableUsedName
						+ "] specified more than once");
			}

			tableUsedNameMap.put(tableUsedName, sqlTable);
			processSqlTableMetadata(tableName);
		}

		return tableUsedNameMap;
	}

	private void processSqlTableMetadata(String tableName) {
		TableView tableView = metadataService.getTableView(appCode, corpCode,
				tableName);
		tableMetadataViewMap.put(tableName, tableView);

		Table mainTable = tableView.getMainTable();
		Map<String, Column> columnMap = mainTable.getColumnMap();
		Collection<Column> columns = columnMap.values();
		for (Column column : columns) {
			column.setTableName(tableName);
		}

		Map<String, CorpTable> extTablesMap = tableView.getExtTables();
		if (extTablesMap == null) {
			return;
		}

		Collection<CorpTable> extTables = extTablesMap.values();
		for (CorpTable corpTable : extTables) {
			String extTableName = corpTable.getExtTableName();
			Map<String, CorpColumn> corpColumnMap = corpTable
					.getCorpColumnMap();
			if (corpColumnMap == null) {
				continue;
			}

			Collection<CorpColumn> extColumns = corpColumnMap.values();
			for (CorpColumn corpColumn : extColumns) {
				corpColumn.setCorpTableNmae(tableName);
				corpColumn.setExtTableName(extTableName);
			}
		}
	}

	/**
	 * 分析列的元数据信息.
	 */
	private void analyzeSqlColumns(
			Map<String, SqlTable> usedTableNameSqlTableMap) {

		List<SqlColumn> sqlColumnList = getSqlColumnList();
		for (SqlColumn sqlColumn : sqlColumnList) {
			String actualColumnName = sqlColumn.getActualColumnName();
			boolean processed = false;
			if (sqlColumn.hasModifier()) {
				String modifier = sqlColumn.getModifier();
				if (actualColumnName.equals(SqlConstants.ASTERISK)) {
					SqlTable sqlTable = usedTableNameSqlTableMap.get(modifier);
					processSingleTableAsteriskColumns(sqlColumn, sqlTable);
					processed = true;
				} else {
					SqlTable sqlTable = usedTableNameSqlTableMap.get(modifier);
					processed = processSqlColumn(sqlTable, sqlColumn);
				}
			} else {
				if (actualColumnName.equals(SqlConstants.ASTERISK)) {
					processAllTableAsteriskColumns(sqlColumn,
							usedTableNameSqlTableMap);
					processed = true;
				} else {
					processed = processUnmodifiedSqlColumn(sqlColumn,
							usedTableNameSqlTableMap);
				}
			}

			if (!processed) {
				throw new MqlParseException("Can't recognize mql column "
						+ sqlColumn);
			}
		}
	}

	private void addSqlTableColumn(SqlTable sqlTable, SqlColumn sqlColumn) {

		List<SqlColumn> sqTablelColumnList = sqlTableColumnMap.get(sqlTable);
		if (sqTablelColumnList == null) {
			sqTablelColumnList = new ArrayList<SqlColumn>();
			sqlTableColumnMap.put(sqlTable, sqTablelColumnList);
		}

		if (!sqTablelColumnList.contains(sqlColumn)) {
			sqTablelColumnList.add(sqlColumn);
		}
	}

	private void processAllTableAsteriskColumns(SqlColumn sqlAsteriskColumn,
			Map<String, SqlTable> sqlTableMap) {

		if (sqlAsteriskColumnMap.containsKey(sqlAsteriskColumn)) {
			return;
		}

		Collection<SqlTable> sqlTableListCollection = sqlTableMap.values();
		for (SqlTable sqlTable : sqlTableListCollection) {
			processSingleTableAsteriskColumns(sqlAsteriskColumn, sqlTable);
		}
	}

	private void processSingleTableAsteriskColumns(SqlColumn sqlAsteriskColumn,
			SqlTable sqlTable) {

		String tableName = sqlTable.getTableName();
		TableView tableView = tableMetadataViewMap.get(tableName);

		List<SqlColumn> asteriskColumnList = sqlAsteriskColumnMap
				.get(sqlAsteriskColumn);
		if (asteriskColumnList == null) {
			asteriskColumnList = new ArrayList<SqlColumn>();
			sqlAsteriskColumnMap.put(sqlAsteriskColumn, asteriskColumnList);
		}

		List<SqlColumn> sqlColumnList = sqlTableColumnMap.get(sqlTable);
		if (sqlColumnList == null) {
			sqlColumnList = new ArrayList<SqlColumn>();
			sqlTableColumnMap.put(sqlTable, sqlColumnList);
		}

		Table mainTable = tableView.getMainTable();
		Map<String, Column> columnMap = mainTable.getColumnMap();
		Collection<Column> mainColumns = columnMap.values();
		for (Column column : mainColumns) {
			String columnName = column.getColumnName();
			SqlColumn sqlColumn = new SqlColumn(columnName, SqlPhase.SELECT);
			if (!asteriskColumnList.contains(sqlColumn)) {
				asteriskColumnList.add(sqlColumn);
				processColumn(sqlTable, sqlColumn, column);
			}
		}
	}

	private void processCorpColumn(SqlColumn sqlColumn, SqlTable sqlTable,
			CorpColumn mdlCorpColumn) {
		String tableName = sqlTable.getTableName();
		String tableAlias = sqlTable.getTableAlias();
		String columnName = sqlColumn.getActualColumnName();
		sqlColumnCorpColumnMap.put(sqlColumn, mdlCorpColumn);

		String extTableName = mdlCorpColumn.getExtTableName();
		SqlTable extSqlTable = prepareExtTable(sqlTable, extTableName);
		extSqlTable.setExtTable(true);
		sqlColumnTableMap.put(sqlColumn, extSqlTable);

		if (sqlColumn.getSqlPhase() == SqlPhase.SELECT) {
			addSqlTableColumn(sqlTable, sqlColumn);
			SqlResultColumn resultColumn = new SqlResultColumn(tableName,
					tableAlias, columnName, true);
			this.sqlColumnResultMap.put(sqlColumn, resultColumn);
		}
	}

	private void processColumn(SqlTable sqlTable, SqlColumn sqlColumn,
			Column mdlColumn) {
		String tableName = sqlTable.getTableName();
		String tableAlias = sqlTable.getTableAlias();
		String columnName = sqlColumn.getActualColumnName();

		sqlColumnColumnMap.put(sqlColumn, mdlColumn);
		sqlColumnTableMap.put(sqlColumn, sqlTable);

		if (sqlColumn.getSqlPhase() == SqlPhase.SELECT) {
			addSqlTableColumn(sqlTable, sqlColumn);
			SqlResultColumn resultColumn = new SqlResultColumn(tableName,
					tableAlias, columnName);
			this.sqlColumnResultMap.put(sqlColumn, resultColumn);
		}
	}

	private boolean processUnmodifiedSqlColumn(SqlColumn sqlColumn,
			Map<String, SqlTable> sqlTableMap) {
		Collection<SqlTable> sqlTableCollection = sqlTableMap.values();
		SqlPhase sqlPhase = sqlColumn.getSqlPhase();
		int columnSliceIndex = sqlColumn.getSliceIndex();
		boolean processed = false;
		for (SqlTable sqlTable : sqlTableCollection) {
			int tableSliceIndex = sqlTable.getSliceIndex();
			if (sqlPhase == SqlPhase.FROM
					&& tableSliceIndex != columnSliceIndex) {
				continue;
			}

			boolean processSqlColumn = processSqlColumn(sqlTable, sqlColumn);
			if (processed && processSqlColumn) {
				throw new MqlParseException("Sql column [" + sqlColumn
						+ "] is ambiguous!");
			}

			if (!processed) {
				processed = processSqlColumn;
			}
		}

		return processed;
	}

	private boolean processSqlColumn(SqlTable sqlTable, SqlColumn sqlColumn) {
		String tableName = sqlTable.getTableName();
		String columnName = sqlColumn.getActualColumnName();
		boolean processed = false;

		TableView tableView = tableMetadataViewMap.get(tableName);
		Table mainTable = tableView.getMainTable();
		Map<String, Column> columnMap = mainTable.getColumnMap();
		if (columnMap.containsKey(columnName)) {
			Column mdlColumn = columnMap.get(columnName);
			processColumn(sqlTable, sqlColumn, mdlColumn);
			processed = true;
		}

		Map<String, CorpTable> extTables = tableView.getExtTables();
		if (extTables == null || extTables.isEmpty()) {
			return processed;
		}
		Collection<CorpTable> corpTabeCollection = extTables.values();
		for (CorpTable corpTable : corpTabeCollection) {
			Map<String, CorpColumn> corpColumnMap = corpTable
					.getCorpColumnMap();
			if (!corpColumnMap.containsKey(columnName)) {
				continue;
			}

			if (processed) {
				throw new MqlParseException("Sql column [" + sqlColumn
						+ "] is ambiguous!");
			} else {
				CorpColumn mdlCorpColumn = corpColumnMap.get(columnName);
				processCorpColumn(sqlColumn, sqlTable, mdlCorpColumn);
				processed = true;
			}
		}

		return processed;
	}

	private SqlTable prepareExtTable(SqlTable sqlTable, String extTableName) {

		Map<String, SqlTable> extTableMap = this.tableAndExtTableMap
				.get(sqlTable);
		if (extTableMap == null) {
			extTableMap = new HashMap<String, SqlTable>();
			this.tableAndExtTableMap.put(sqlTable, extTableMap);
		}

		SqlTable extSqlTable = extTableMap.get(extTableName);
		if (extSqlTable == null) {
			int sliceIndex = sqlTable.getSliceIndex();
			String extTableAlias = this.aliasGenerator.generate(extTableName);
			extSqlTable = new SqlTable(extTableName, extTableAlias, sliceIndex);
			extTableMap.put(extTableName, extSqlTable);
		}

		return extSqlTable;
	}

	private void analyzeSqlTableRelation(
			Map<String, SqlTable> usedTableNameSqlTableMap) {
		Set<SqlTable> sqlTableSet = sqlTableColumnMap.keySet();
		for (SqlTable fromSqlTable : sqlTableSet) {
			String fromTableName = fromSqlTable.getTableName();
			for (SqlTable toSqlTable : sqlTableSet) {
				String toTableName = toSqlTable.getTableName();
				TableRelation tableRelation = metadataService.getTableRelation(
						appCode, corpCode, fromTableName, toTableName);
				if (tableRelation == null) {
					continue;
				}

				String tableRelationship = tableRelation.getRelationship();
				if (TableRelation.ONE_TO_ONE
						.equalsIgnoreCase(tableRelationship)
						|| TableRelation.ONE_TO_MANY
								.equalsIgnoreCase(tableRelationship)
						|| TableRelation.MANY_TO_ONE
								.equalsIgnoreCase(tableRelationship)) {

					processSimpleRel(fromSqlTable, toSqlTable, tableRelation);
				} else if (TableRelation.MANY_TO_MANY
						.equalsIgnoreCase(tableRelationship)) {
					processComplexRel(usedTableNameSqlTableMap, fromSqlTable,
							toSqlTable, tableRelation);
				} else {
					LOG.warn("The relationship[" + tableRelationship
							+ "] doesn't supported!");
					break;
				}
			}
		}
	}

	private void processComplexRel(
			Map<String, SqlTable> usedTableNameSqlTableMap,
			SqlTable fromSqlTable, SqlTable toSqlTable,
			TableRelation tableRelation) {
		Collection<SqlTable> sqlTableCollection = usedTableNameSqlTableMap
				.values();
		String relTableName = tableRelation.getRelTableName();
		SqlTable relTable = null;
		for (SqlTable sqlTable : sqlTableCollection) {
			String tableName = sqlTable.getTableName();
			if (tableName.equalsIgnoreCase(relTableName)) {
				relTable = sqlTable;
				break;
			}
		}

		if (relTable == null) {
			return;
		}

		processSimpleRel(fromSqlTable, toSqlTable, tableRelation);
		String relToColumnName = tableRelation.getRelToColumnName();
		processSqlTablePk(relTable);
		processSqlTableRelationColumn(relTable, relToColumnName);
	}

	private void processSimpleRel(SqlTable fromSqlTable, SqlTable toSqlTable,
			TableRelation tableRelation) {
		String fromColumnName = tableRelation.getFromColumnName();
		processSqlTablePk(fromSqlTable);
		processSqlTableRelationColumn(fromSqlTable, fromColumnName);

		String toColumnName = tableRelation.getToColumnName();
		processSqlTablePk(toSqlTable);
		processSqlTableRelationColumn(toSqlTable, toColumnName);
	}

	private void processSqlTablePk(SqlTable sqlTable) {
		String fromTableName = sqlTable.getTableName();
		TableView tableView = tableMetadataViewMap.get(fromTableName);
		Table mainTable = tableView.getMainTable();
		String pkName = mainTable.getPkName();

		processSqlTableRelationColumn(sqlTable, pkName);
	}

	private void processSqlTableRelationColumn(SqlTable sqlTable,
			String columnName) {
		SqlColumn relSqlColumn = new SqlColumn(columnName, SqlPhase.SELECT);
		List<SqlColumn> fromSqlColumnList = sqlTableColumnMap.get(sqlTable);
		for (SqlColumn sqlColumn : fromSqlColumnList) {
			String actualColumnName = sqlColumn.getActualColumnName();
			if (actualColumnName.equals(columnName)) {
				return;
			}
		}

		relSqlColumn.setRelMark(true);
		processSqlColumn(sqlTable, relSqlColumn);
		sqlTableRelColumnList.add(relSqlColumn);
	}

	public SqlTable getBelongToSqlTable(SqlColumn sqlColumn) {
		return sqlColumnTableMap.get(sqlColumn);
	}

	/**
	 * 这个方法返回所有包含在结果集中出现列的表.<br>
	 * 这个方法必须在analyze方法之后调用
	 * 
	 * @return
	 */
	public List<SqlTable> getSqlTableInQueryResults() {
		Set<SqlTable> sqlTableSet = sqlTableColumnMap.keySet();

		return new ArrayList<SqlTable>(sqlTableSet);
	}

	public abstract List<SqlTable> getSqlTableList();

	public abstract List<SqlColumn> getSqlColumnList();

	public abstract String getRealColumnName(SqlColumn sqlColumn);

	public abstract String getRealTableName(SqlTable sqlTable);

	public abstract String getTablePrimaryKey(String realTableName);

	// 下面是自动生成的Get和Set方法

	public SqlAliasGenerator getAliasGenerator() {
		return aliasGenerator;
	}

	public void setAliasGenerator(SqlAliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	public Map<String, TableView> getTableMetadataViewMap() {
		return tableMetadataViewMap;
	}

	public void setTableMetadataViewMap(
			Map<String, TableView> tableMetadataViewMap) {
		this.tableMetadataViewMap = tableMetadataViewMap;
	}

	public Map<SqlColumn, SqlResultColumn> getSqlColumnResultMap() {
		return sqlColumnResultMap;
	}

	public void setSqlColumnResultMap(
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap) {
		this.sqlColumnResultMap = sqlColumnResultMap;
	}

	public Map<SqlColumn, SqlTable> getSqlColumnTableMap() {
		return sqlColumnTableMap;
	}

	public void setSqlColumnTableMap(Map<SqlColumn, SqlTable> sqlColumnTableMap) {
		this.sqlColumnTableMap = sqlColumnTableMap;
	}

	public Map<SqlColumn, Column> getSqlColumnColumnMap() {
		return sqlColumnColumnMap;
	}

	public void setSqlColumnColumnMap(Map<SqlColumn, Column> sqlColumnColumnMap) {
		this.sqlColumnColumnMap = sqlColumnColumnMap;
	}

	public Map<SqlColumn, CorpColumn> getSqlColumnCorpColumnMap() {
		return sqlColumnCorpColumnMap;
	}

	public void setSqlColumnCorpColumnMap(
			Map<SqlColumn, CorpColumn> sqlColumnCorpColumnMap) {
		this.sqlColumnCorpColumnMap = sqlColumnCorpColumnMap;
	}

	public List<SqlColumn> getSqlAsteriskColumns(SqlColumn sqlAsterisk) {
		return sqlAsteriskColumnMap.get(sqlAsterisk);
	}

	public Map<SqlTable, Map<String, SqlTable>> getTableAndExtTableMap() {
		return tableAndExtTableMap;
	}

	public Map<SqlTable, List<SqlColumn>> getSqlTableColumnMap() {
		return sqlTableColumnMap;
	}

	public void setSqlTableColumnMap(
			Map<SqlTable, List<SqlColumn>> sqlTableColumnMap) {
		this.sqlTableColumnMap = sqlTableColumnMap;
	}

	public Map<SqlColumn, List<SqlColumn>> getSqlAsteriskColumnMap() {
		return sqlAsteriskColumnMap;
	}

	public void setSqlAsteriskColumnMap(
			Map<SqlColumn, List<SqlColumn>> sqlAsteriskColumnMap) {
		this.sqlAsteriskColumnMap = sqlAsteriskColumnMap;
	}

	public List<SqlColumn> getSqlTableRelColumnList() {
		return sqlTableRelColumnList;
	}

	public void setSqlTableRelColumnList(List<SqlColumn> sqlTableRelColumnList) {
		this.sqlTableRelColumnList = sqlTableRelColumnList;
	}

	public void setTableAndExtTableMap(
			Map<SqlTable, Map<String, SqlTable>> tableAndExtTableMap) {
		this.tableAndExtTableMap = tableAndExtTableMap;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public String getCorpCode() {
		return corpCode;
	}

	public void setCorpCode(String corpCode) {
		this.corpCode = corpCode;
	}

	public MqlMetadataService getMetadataService() {
		return metadataService;
	}

	public void setMetadataService(MqlMetadataService metadataService) {
		this.metadataService = metadataService;
	}

	public boolean isCareRelation() {
		return careRelation;
	}

	public void setCareRelation(boolean careRelation) {
		this.careRelation = careRelation;
	}
}
