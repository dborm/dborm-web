package com.tbc.paas.mql.parser;

import static com.tbc.paas.mql.util.SqlConstants.DOT;
import static com.tbc.paas.mql.util.SqlConstants.EQUAL;
import static com.tbc.paas.mql.util.SqlConstants.LEFT_JOIN;
import static com.tbc.paas.mql.util.SqlConstants.ON;

import java.util.List;
import java.util.Map;

import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.notify.MqlNotify;
import com.tbc.paas.mql.util.SqlBuilder;

public abstract class MqlParser {

	protected static boolean oracle = false;
	protected MqlNotify mqlNotify;
	protected MqlAnalyzer mqlAnalyzer;
	protected SqlGrammar sqlGrammar;
	protected MqlPkCallback mqlPkCallback;

	protected List<Object> parameterList;
	protected Map<String, Object> parameterMap;

	public MqlParser() {
		super();
		init();
	}

	public MqlParser(SqlGrammar sqlGrammar) {
		super();
		this.sqlGrammar = sqlGrammar;
		init();
	}

	public MqlParser(SqlGrammar sqlGrammar, List<Object> parameterList) {
		super();
		this.sqlGrammar = sqlGrammar;
		this.parameterList = parameterList;
		init();
	}

	public MqlParser(SqlGrammar sqlGrammar, Map<String, Object> parameterMap) {
		super();
		this.sqlGrammar = sqlGrammar;
		this.parameterMap = parameterMap;
		init();
	}

	public MqlParser(SqlGrammar sqlGrammar, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super();
		this.sqlGrammar = sqlGrammar;
		this.parameterList = parameterList;
		this.parameterMap = parameterMap;
		init();
	}

	private void init() {
		mqlNotify = new MqlNotify();
	}

	public abstract List<SqlBuilder> parse();

	public List<Object> getParameterList() {
		return parameterList;
	}

	public void setParameterList(List<Object> parameterList) {
		this.parameterList = parameterList;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	protected List<Object> executePkQuery(SqlBuilder sqlBuilder) {

		return mqlPkCallback.queryPrimaryKey(sqlBuilder);
	}

	public SqlGrammar getSqlGrammar() {
		return sqlGrammar;
	}

	public void setSqlGrammar(SqlGrammar sqlGrammar) {
		this.sqlGrammar = sqlGrammar;
	}

	public MqlPkCallback getMqlPkCallback() {
		return mqlPkCallback;
	}

	public void setMqlPkCallback(MqlPkCallback mqlPkCallback) {
		this.mqlPkCallback = mqlPkCallback;
	}

	public MqlAnalyzer getMqlAnalyzer() {
		return mqlAnalyzer;
	}

	public void setMqlAnalyzer(MqlAnalyzer mqlAnalyzer) {
		this.mqlAnalyzer = mqlAnalyzer;
	}

	protected SqlBuilder processSqlTableExt(SqlTable sqlTable,
			String realTableName) {

		SqlBuilder builder = new SqlBuilder();
		Map<SqlTable, Map<String, SqlTable>> tableAndExtTableMap = mqlAnalyzer
				.getTableAndExtTableMap();
		Map<String, SqlTable> extTableMap = tableAndExtTableMap.get(sqlTable);
		if (extTableMap == null) {
			return builder;
		}

		String tableAlias = sqlTable.getTableAlias();
		String tablePrimaryKey = mqlAnalyzer.getTablePrimaryKey(realTableName);

		for (SqlTable extTable : extTableMap.values()) {
			String extTableName = extTable.getTableName();
			if (extTableName.equalsIgnoreCase(realTableName)) {
				continue;
			}
			String extTableAlias = extTable.getTableAlias();
			String extPk = mqlAnalyzer.getTablePrimaryKey(extTableName);
			builder.append(LEFT_JOIN).append(extTableName)
					.append(extTableAlias).append(ON).append(tableAlias)
					.disableSlipt().append(DOT).append(tablePrimaryKey)
					.enableSlipt().append(EQUAL).append(extTableAlias)
					.disableSlipt().append(DOT).append(extPk).enableSlipt();
		}

		return builder;
	}

	public static boolean isOracle() {
		return oracle;
	}

	public static void setOracle(boolean oracle) {
		MqlParser.oracle = oracle;
	}

	public MqlNotify getMqlNotify() {
		return mqlNotify;
	}

	public void setMqlNotify(MqlNotify mqlNotify) {
		this.mqlNotify = mqlNotify;
	}
}
