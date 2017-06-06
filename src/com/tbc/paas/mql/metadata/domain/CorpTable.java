package com.tbc.paas.mql.metadata.domain;

import java.util.HashMap;
import java.util.Map;


public class CorpTable {
	private String extTableName;
	private String extPkName;
	private Map<String, CorpColumn> corpColumnMap;

	public CorpTable() {
		super();
		corpColumnMap = new HashMap<String, CorpColumn>();
	}

	public String getExtTableName() {
		return extTableName;
	}

	public void setExtTableName(String extTableName) {
		this.extTableName = extTableName;
	}

	public String getExtPkName() {
		return extPkName;
	}

	public void setExtPkName(String extPkName) {
		this.extPkName = extPkName;
	}

	public Map<String, CorpColumn> getCorpColumnMap() {
		return corpColumnMap;
	}

	public void setCorpColumnMap(Map<String, CorpColumn> corpColumnMap) {
		this.corpColumnMap = corpColumnMap;
	}
}
