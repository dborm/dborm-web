package com.tbc.paas.mdl.attach;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.mql.MdlAnalyzer;
import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.parser.MqlSelectParser;
import com.tbc.paas.mql.parser.attach.MqlSelectAttach;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;

public class MqlSelectAttachImpl extends AbstractMqlAttach implements
		MqlSelectAttach {

	public MqlSelectAttachImpl() {
		super();
	}

	@Override
	public SqlBuilder getColumnAttach(MqlSelectParser selectParser) {
		MqlAnalyzer analyzer = selectParser.getMqlAnalyzer();
		if (!(analyzer instanceof MdlAnalyzer)) {
			return null;
		}
		MdlAnalyzer mdlAnalyzer = (MdlAnalyzer) analyzer;

		SqlBuilder sqlBuilder = new SqlBuilder();
		Map<SqlTable, List<SqlColumn>> extraSqlTableColumnMap = mdlAnalyzer
				.getExtraSqlTableColumnMap();
		if (extraSqlTableColumnMap == null
				|| extraSqlTableColumnMap.size() == 0) {
			return sqlBuilder;
		}

		sqlBuilder.removeLastSlice(SqlConstants.COMMA);

		Collection<List<SqlColumn>> sqlColumnCollection = extraSqlTableColumnMap
				.values();
		for (List<SqlColumn> sqlColumnList : sqlColumnCollection) {
			for (SqlColumn sqlColumn : sqlColumnList) {
				String realColumnName = mdlAnalyzer
						.getRealColumnName(sqlColumn);
				sqlBuilder.append(realColumnName).append(SqlConstants.COMMA);
			}
		}

		sqlBuilder.append(SqlConstants.COMMA);

		return sqlBuilder;
	}

}
