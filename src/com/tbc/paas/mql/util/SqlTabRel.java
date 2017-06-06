package com.tbc.paas.mql.util;

import java.util.ArrayList;
import java.util.List;

public class SqlTabRel {
	public static final String ONE_TO_ONE = "OTO";
	public static final String ONE_TO_MANY = "OTM";
	public static final String MANY_TO_ONE = "MTO";
	public static final String MANY_TO_MANY = "MTM";

	public String relation = ONE_TO_ONE;

	public String fromTable;
	public String fromColumn;

	public String toTable;
	public String toColumn;

	public String relTable;
	public String relFromColumn;
	public String relToColumn;

	public List<SqlDupRel> dulRelList = new ArrayList<SqlDupRel>();

	public SqlTabRel() {
		super();
	}
	
	

	public SqlTabRel(String relation, String fromTable, String fromColumn,
			String toTable, String toColumn) {
		super();
		this.relation = relation;
		this.fromTable = fromTable;
		this.fromColumn = fromColumn;
		this.toTable = toTable;
		this.toColumn = toColumn;
	}


	public List<SqlDupRel> getDulRelList() {
		return dulRelList;
	}

	public void setDulRelList(List<SqlDupRel> dulRelList) {
		this.dulRelList = dulRelList;
	}

	@Override
	public String toString() {

		return "SqlTabRel [rs=" + relation + ", f=" + fromTable
				+ ", t=" + toTable + ", r=" + relTable + "]";
		
	
	}

}
