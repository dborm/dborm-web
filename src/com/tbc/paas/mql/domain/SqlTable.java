package com.tbc.paas.mql.domain;

public class SqlTable {
	private String tableName;
	private String tableAlias;
	private int sliceIndex;
	private boolean extTable;

	public SqlTable() {
		super();
	}

	public SqlTable(String tableName) {
		super();
		this.tableName = tableName;
	}

	public SqlTable(String tableName, String tableAlias, int sliceIndex) {
		super();
		this.tableName = tableName;
		this.tableAlias = tableAlias;
		this.sliceIndex = sliceIndex;
	}

	public SqlTable(String tableName, String tableAlias, boolean extTable) {
		super();
		this.tableName = tableName;
		this.tableAlias = tableAlias;
		this.extTable = extTable;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public int getSliceIndex() {
		return sliceIndex;
	}

	public void setSliceIndex(int sliceIndex) {
		this.sliceIndex = sliceIndex;
	}

	public boolean isExtTable() {
		return extTable;
	}

	public void setExtTable(boolean extTable) {
		this.extTable = extTable;
	}

	@Override
	public String toString() {
		return "tableName=" + tableName + ", tableAlias=" + tableAlias
				+ ", sliceIndex=" + sliceIndex;
	}

}
