package com.tbc.paas.mql.metadata.domain;

import java.util.Map;



public class TableView {
	private Table mainTable;
	private Map<String, CorpTable> extTables;

	public TableView() {
		super();
	}

	public TableView(Table mainTable, Map<String, CorpTable> extTables) {
		super();
		this.mainTable = mainTable;
		this.extTables = extTables;
	}

	public Table getMainTable() {
		return mainTable;
	}

	public void setMainTable(Table mainTable) {
		this.mainTable = mainTable;
	}

	public Map<String, CorpTable> getExtTables() {
		return extTables;
	}

	public void setExtTables(Map<String, CorpTable> extTables) {
		this.extTables = extTables;
	}
}
