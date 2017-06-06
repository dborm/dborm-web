package com.tbc.paas.mdl.attach;

import static com.tbc.paas.mql.util.SqlConstants.COMMA;
import static com.tbc.paas.mql.util.SqlConstants.QUESTION;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.parser.attach.MqlInsertAttach;
import com.tbc.paas.mql.util.SqlBuilder;

;

public class MqlInsertAttachImpl extends AbstractMqlAttach implements
		MqlInsertAttach {

	public MqlInsertAttachImpl() {
		super();
		init();
	}

	private void init() {
	}

	@Override
	public SqlBuilder getColumnAttach(MqlAnalyzer analyzer) {

		List<SqlColumn> sqlColumnList = analyzer.getSqlColumnList();
		Set<String> realColumnSet = new HashSet<String>();
		for (SqlColumn sqlColumn : sqlColumnList) {
			String realColumnName = analyzer.getRealColumnName(sqlColumn);
			realColumnSet.add(realColumnName);
		}

		SqlBuilder columnAttach = new SqlBuilder();
		appendColumn(realColumnSet, columnAttach, CREATE_TIME);
		appendColumn(realColumnSet, columnAttach, CREATE_BY);
		appendColumn(realColumnSet, columnAttach, LAST_MODIFY_TIME);
		appendColumn(realColumnSet, columnAttach, LAST_MODIFY_BY);
		appendColumn(realColumnSet, columnAttach, OPT_TIME);
		columnAttach.removeLastSlice(COMMA);

		return columnAttach;
	}

	private void appendColumn(Set<String> realColumnSet,
			SqlBuilder columnAttach, String value) {
		if (!realColumnSet.contains(value)) {
			columnAttach.append(value, COMMA);
		}
	}

	@Override
	public SqlBuilder getValueAttach(MqlAnalyzer analyzer) {
		SqlBuilder valueAttach = new SqlBuilder();
		List<SqlColumn> sqlColumnList = analyzer.getSqlColumnList();
		Set<String> realColumnSet = new HashSet<String>();
		for (SqlColumn sqlColumn : sqlColumnList) {
			String realColumnName = analyzer.getRealColumnName(sqlColumn);
			realColumnSet.add(realColumnName);
		}

		long currentTimeMillis = System.currentTimeMillis();
		long optTime = currentTimeMillis / OPT_TIME_INTERVAL;
		Timestamp createTimestamp = new Timestamp(currentTimeMillis);
		String userId = ExecutionContext.getUserId();
		if (!realColumnSet.contains(CREATE_TIME)) {
			valueAttach.append(QUESTION, COMMA).addParameter(createTimestamp);
		}

		if (!realColumnSet.contains(CREATE_BY)) {
			valueAttach.append(QUESTION, COMMA).addParameter(userId);
		}

		if (!realColumnSet.contains(LAST_MODIFY_TIME)) {
			valueAttach.append(QUESTION, COMMA).addParameter(createTimestamp);
		}

		if (!realColumnSet.contains(LAST_MODIFY_BY)) {
			valueAttach.append(QUESTION, COMMA).addParameter(userId);
		}

		if (!realColumnSet.contains(OPT_TIME)) {
			valueAttach.append(QUESTION, COMMA).addParameter(optTime);
		}

		valueAttach.removeLastSlice(COMMA);

		return valueAttach;
	}
}
