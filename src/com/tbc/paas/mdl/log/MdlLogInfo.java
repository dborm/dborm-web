package com.tbc.paas.mdl.log;

import com.tbc.paas.mql.util.SqlBuilder;

public class MdlLogInfo {

	// sql开始执行时间。
	private long startTime;
	// sql执行的时间
	private long costTime;
	// 实际执行的sql
	private SqlBuilder sqlBuilder;
	// 标示SQL是否在共享共享执行
	private boolean shared;


	public MdlLogInfo() {
		this(System.currentTimeMillis(), null, false);
	}

	public MdlLogInfo(SqlBuilder sqlBuilder) {
		this(System.currentTimeMillis(), sqlBuilder, false);
	}

	public MdlLogInfo(SqlBuilder sqlBuilder, boolean shared) {
		this(System.currentTimeMillis(), sqlBuilder, shared);
	}

	public MdlLogInfo(long startTime, SqlBuilder sqlBuilder, boolean shared) {
		super();
		this.startTime = startTime;
		this.sqlBuilder = sqlBuilder;
		this.shared = shared;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getCostTime() {
		return costTime;
	}

	public void setCostTime(long costTime) {
		this.costTime = costTime;
	}

	public SqlBuilder getSqlBuilder() {
		return sqlBuilder;
	}

	public void setSqlBuilder(SqlBuilder sqlBuilder) {
		this.sqlBuilder = sqlBuilder;
	}

	public void countCostTime() {
		this.costTime = System.currentTimeMillis() - startTime;
	}
}
