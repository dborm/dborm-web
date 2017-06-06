package com.tbc.paas.mql.metadata.domain;

public class TableRelation {
	public static final String ONE_TO_ONE = "onetoone";
	public static final String ONE_TO_MANY = "onetomany";
	public static final String MANY_TO_ONE = "manytoone";
	public static final String MANY_TO_MANY = "manytomany";

	private String relationship;
	private String fromTableName;
	private String fromColumnName;
	private String toTableName;
	private String toColumnName;
	private String relTableName;
	private String relFromColumnName;
	private String relToColumnName;

	public TableRelation() {
		super();
	}

	public TableRelation(String relationship, String fromTableName,
			String fromColumnName, String toTableName, String toColumnName) {
		super();
		this.relationship = relationship;
		this.fromTableName = fromTableName;
		this.fromColumnName = fromColumnName;
		this.toTableName = toTableName;
		this.toColumnName = toColumnName;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getFromTableName() {
		return fromTableName;
	}

	public void setFromTableName(String fromTableName) {
		this.fromTableName = fromTableName;
	}

	public String getFromColumnName() {
		return fromColumnName;
	}

	public void setFromColumnName(String fromColumnName) {
		this.fromColumnName = fromColumnName;
	}

	public String getToTableName() {
		return toTableName;
	}

	public void setToTableName(String toTableName) {
		this.toTableName = toTableName;
	}

	public String getToColumnName() {
		return toColumnName;
	}

	public void setToColumnName(String toColumnName) {
		this.toColumnName = toColumnName;
	}

	public String getRelTableName() {
		return relTableName;
	}

	public void setRelTableName(String relTableName) {
		this.relTableName = relTableName;
	}

	public String getRelFromColumnName() {
		return relFromColumnName;
	}

	public void setRelFromColumnName(String relFromColumnName) {
		this.relFromColumnName = relFromColumnName;
	}

	public String getRelToColumnName() {
		return relToColumnName;
	}

	public void setRelToColumnName(String relToColumnName) {
		this.relToColumnName = relToColumnName;
	}
}
