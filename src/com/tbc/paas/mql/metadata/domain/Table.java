package com.tbc.paas.mql.metadata.domain;

import java.util.Map;

public class Table {
	// 标示这个表是不是在共享中心。
	private boolean shared = false;
	private String tableName;
	private String pkName;
	private Map<String, Column> columnMap;

	public Table() {
		super();
	}

	public Table(String tableName) {
		super();
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPkName() {
		return pkName;
	}

	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

	public Map<String, Column> getColumnMap() {
		return columnMap;
	}

	public void setColumnMap(Map<String, Column> columnMap) {
		this.columnMap = columnMap;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
}
