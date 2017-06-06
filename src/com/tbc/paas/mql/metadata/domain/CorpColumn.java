package com.tbc.paas.mql.metadata.domain;

public class CorpColumn {
	private String extTableName;
	private String extColumnName;
	private String extColumnType;
	private boolean extNullable;

	private String corpTableNmae;
	private String corpColumnName;
	private String corpColumnType;

	public String getExtTableName() {
		return extTableName;
	}

	public void setExtTableName(String extTableName) {
		this.extTableName = extTableName;
	}

	public String getCorpTableNmae() {
		return corpTableNmae;
	}

	public void setCorpTableNmae(String corpTableNmae) {
		this.corpTableNmae = corpTableNmae;
	}

	public String getExtColumnName() {
		return extColumnName;
	}

	public void setExtColumnName(String extColumnName) {
		this.extColumnName = extColumnName;
	}

	public String getExtColumnType() {
		return extColumnType;
	}

	public void setExtColumnType(String extColumnType) {
		this.extColumnType = extColumnType;
	}

	public boolean isExtNullable() {
		return extNullable;
	}

	public void setExtNullable(boolean extNullable) {
		this.extNullable = extNullable;
	}

	public String getCorpColumnName() {
		return corpColumnName;
	}

	public void setCorpColumnName(String corpColumnName) {
		this.corpColumnName = corpColumnName;
	}

	public String getCorpColumnType() {
		return corpColumnType;
	}

	public void setCorpColumnType(String corpColumnType) {
		this.corpColumnType = corpColumnType;
	}
}
