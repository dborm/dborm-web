package com.tbc.paas.mql.domain;

import com.tbc.paas.mql.util.SqlConstants;

public class SqlColumn {

	private String columnName;
	private int sliceIndex;
	private SqlPhase sqlPhase;
	private boolean relMark;

	public SqlColumn() {
		super();
	}

	public SqlColumn(String columnName, SqlPhase sqlPhase) {
		super();
		this.columnName = columnName;
		this.sqlPhase = sqlPhase;
	}

	public SqlColumn(String columnName, SqlPhase sqlPhase, int sliceIndex) {
		super();
		this.columnName = columnName;
		this.sqlPhase = sqlPhase;
		this.sliceIndex = sliceIndex;
	}

	public SqlColumn(String actualColumnName) {
		this(actualColumnName, null, 0);
	}

	public int getSliceIndex() {
		return sliceIndex;
	}

	public void setSliceIndex(int sliceIndex) {
		this.sliceIndex = sliceIndex;
	}

	public SqlPhase getSqlPhase() {
		return sqlPhase;
	}

	public void setSqlPhase(SqlPhase sqlPhase) {
		this.sqlPhase = sqlPhase;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean hasModifier() {
		if (columnName == null) {
			return false;
		}

		return -1 != columnName.lastIndexOf(SqlConstants.DOT);
	}

	public String getModifier() {
		int dotIndex = columnName.lastIndexOf(SqlConstants.DOT);
		return columnName.substring(0, dotIndex);
	}

	public String getActualColumnName() {
		int dotIndex = columnName.lastIndexOf(SqlConstants.DOT);
		return columnName.substring(dotIndex + SqlConstants.DOT.length());
	}

	public boolean isRelMark() {
		return relMark;
	}

	public void setRelMark(boolean relMark) {
		this.relMark = relMark;
	}

	@Override
	public String toString() {
		return "columnName=" + columnName + ", sliceIndex=" + sliceIndex
				+ ", sqlPhase=" + sqlPhase;
	}
}
