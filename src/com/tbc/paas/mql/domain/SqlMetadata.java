package com.tbc.paas.mql.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlMetadata {

	private boolean queryAllColumn;
	private List<SqlTable> sqlTableList;
	private Map<SqlPhase, List<SqlColumn>> sqlTableColumnMap;

	public SqlMetadata() {
		super();
		sqlTableList = new ArrayList<SqlTable>();
		sqlTableColumnMap = new HashMap<SqlPhase, List<SqlColumn>>();
	}

	public void addTable(SqlTable sqlTable) {
		if (sqlTable == null) {
			throw new IllegalArgumentException("Table name mustn't be null!");
		}

		if (this.sqlTableList.contains(sqlTable)) {
			return;
		}

		this.sqlTableList.add(sqlTable);
	}

	public void addColumn(SqlColumn column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"Column name or phase mustn't be null!");
		}

		SqlPhase sqlPhase = column.getSqlPhase();
		List<SqlColumn> columnList = sqlTableColumnMap.get(sqlPhase);
		if (columnList == null) {
			columnList = new ArrayList<SqlColumn>();
			sqlTableColumnMap.put(sqlPhase, columnList);
		}

		if (!columnList.contains(column)) {
			columnList.add(column);
		}
	}

	public List<SqlColumn> getColumnList(SqlPhase phase) {

		return this.sqlTableColumnMap.get(phase);
	}

	public List<SqlColumn> getAllColumns() {
		Collection<List<SqlColumn>> columns = this.sqlTableColumnMap.values();
		Set<SqlColumn> columnList = new HashSet<SqlColumn>();
		for (List<SqlColumn> phaseColumns : columns) {
			columnList.addAll(phaseColumns);
		}
		return new ArrayList<SqlColumn>(columnList);
	}

	/**
	 * 如果当前操作中只存在一个表,则可以用这种方式获取.
	 * 
	 * @return 有且仅有的一个SqlTable
	 */
	public SqlTable getUniqueTable() {
		if (this.sqlTableList == null) {
			throw new MqlParseException("No sql tabe exist!");
		}

		if (this.sqlTableList.size() != 1) {
			throw new MqlParseException("more than one  sql tabe exist!");
		}

		return this.sqlTableList.get(0);
	}

	/**
	 * 或得所有SqlTable
	 * 
	 * @return 所有SqlTable
	 */
	public List<SqlTable> getSqlTableList() {
		return this.sqlTableList;
	}

	public SqlTable getSqlTable(String tableName, int sliceIndex) {
		if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Table name mustn't be empty!");
		}

		for (SqlTable sqlTable : sqlTableList) {
			if (sqlTable.getTableName().equalsIgnoreCase(tableName)
					&& sqlTable.getSliceIndex() == sliceIndex)
				return sqlTable;
		}

		return null;
	}

	public boolean isQueryAllColumn() {
		return queryAllColumn;
	}

	public void setQueryAllColumn(boolean queryAllColumn) {
		this.queryAllColumn = queryAllColumn;
	}

	public void clear() {
		this.sqlTableList.clear();
		this.sqlTableColumnMap.clear();
	}
}
