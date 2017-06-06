package com.tbc.paas.mdl.attach;

import static com.tbc.paas.mql.util.SqlConstants.COMMA;
import static com.tbc.paas.mql.util.SqlConstants.EQUAL;
import static com.tbc.paas.mql.util.SqlConstants.QUESTION;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.parser.attach.MqlUpdateAttach;
import com.tbc.paas.mql.util.SqlBuilder;

public class MqlUpdateAttachImpl extends AbstractMqlAttach implements
		MqlUpdateAttach {

	public MqlUpdateAttachImpl() {
		super();
	}

	@Override
	public SqlBuilder getColumnAttachPart(MqlAnalyzer mqlAnalyzer) {
		long currentTimeMillis = System.currentTimeMillis();
		Timestamp createTimestamp = new Timestamp(currentTimeMillis);
		String userId = ExecutionContext.getUserId();

		List<SqlColumn> sqlColumnList = mqlAnalyzer.getSqlColumnList();
		Set<String> realColumnSet = new HashSet<String>();
		for (SqlColumn sqlColumn : sqlColumnList) {
			String realColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
			realColumnSet.add(realColumnName);
		}

		SqlBuilder columnAttach = new SqlBuilder();
		if (!realColumnSet.contains(LAST_MODIFY_TIME)) {
			columnAttach.append(LAST_MODIFY_TIME, EQUAL, QUESTION, COMMA)
					.addParameter(createTimestamp);
		}

		if (!realColumnSet.contains(LAST_MODIFY_BY)) {
			columnAttach.append(LAST_MODIFY_BY, EQUAL, QUESTION, COMMA)
					.addParameter(userId);
		}

		columnAttach.removeLastSlice(COMMA);

		return columnAttach;
	}

}
