package com.tbc.paas.mql.domain;

public class SqlResultColumn {
	private String tableName;
	private String tableAlias;
	private String columnName;
	private String columnAlias;
	private boolean extended;
	private boolean aggregation;

	public SqlResultColumn() {
		super();
	}

	public SqlResultColumn(String tableName, String tableAlias,
			String columnName) {
		this(tableName, tableAlias, columnName, false);
	}

	public SqlResultColumn(String tableName, String tableAlias,
			String columnName, boolean extended) {
		super();
		this.tableName = tableName;
		this.tableAlias = tableAlias;
		this.columnName = columnName;
		this.extended = extended;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isExtended() {
		return extended;
	}

	public void setExtended(boolean extended) {
		this.extended = extended;
	}

	public boolean isAggregation() {
		return aggregation;
	}

	public void setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public String getColumnAlias() {
		return columnAlias;
	}

	public void setColumnAlias(String columnAlias) {
		this.columnAlias = columnAlias;
	}

	@Override
	public String toString() {
		return " [columnName=" + columnName + "]";
	}
}
