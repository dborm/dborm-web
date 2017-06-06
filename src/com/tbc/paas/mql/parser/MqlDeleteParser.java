package com.tbc.paas.mql.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mql.domain.SqlMetadata;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.TableView;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlBuilder;
import static com.tbc.paas.mql.util.SqlConstants.*;

public class MqlDeleteParser extends MqlParser {

	public MqlDeleteParser() {
		super();
	}

	public MqlDeleteParser(SqlGrammar sqlGrammar, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterList, parameterMap);
	}

	public MqlDeleteParser(SqlGrammar sqlGrammar, List<Object> parameterList) {
		super(sqlGrammar, parameterList);
	}

	public MqlDeleteParser(SqlGrammar sqlGrammar,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterMap);
	}

	public MqlDeleteParser(SqlGrammar sqlGrammar) {
		super(sqlGrammar);
	}

	@Override
	public List<SqlBuilder> parse() {
		mqlAnalyzer.analyze();
		List<SqlBuilder> resultSqls = new ArrayList<SqlBuilder>();
		SqlBuilder sqlSelectBuilder = generateSelectSql();
		List<Object> deletePks = executePkQuery(sqlSelectBuilder);
		if (deletePks == null || deletePks.isEmpty()) {
			return resultSqls;
		}

		mqlNotify.setAffectedPrimaryKeyValueList(deletePks);

		List<SqlBuilder> allTableDeleteSqls = generateAllTableDeleteSqls(deletePks);
		resultSqls.addAll(allTableDeleteSqls);

		return resultSqls;
	}

	private List<SqlBuilder> generateAllTableDeleteSqls(List<Object> deletePks) {

		List<SqlBuilder> deleteSqls = new ArrayList<SqlBuilder>();
		SqlMetadata sqlMetadata = sqlGrammar.getSqlMetadata();
		SqlTable deleteSqlTable = sqlMetadata.getUniqueTable();
		String realTableName = mqlAnalyzer.getRealTableName(deleteSqlTable);
		SqlBuilder sqlBuilder = generateMainDeleteSql(deleteSqlTable,
				realTableName, deletePks);
		deleteSqls.add(sqlBuilder);

		Map<String, TableView> tableMetadataViewMap = mqlAnalyzer
				.getTableMetadataViewMap();
		String tableName = deleteSqlTable.getTableName();
		TableView mdlTableView = tableMetadataViewMap.get(tableName);
		Map<String, CorpTable> extTableMap = mdlTableView.getExtTables();
		if (extTableMap == null) {
			return deleteSqls;
		}

		Collection<CorpTable> values = extTableMap.values();
		for (CorpTable corpTable : values) {
			String extTableName = corpTable.getExtTableName();
			if (realTableName.equalsIgnoreCase(extTableName)) {
				continue;
			}

			SqlBuilder extDeleteSql = generateExtDeleteSql(corpTable, deletePks);
			deleteSqls.add(extDeleteSql);
		}

		return deleteSqls;
	}

	private SqlBuilder generateMainDeleteSql(SqlTable sqlTable,
			String realTableName, List<Object> deletePks) {

		String mainTablePk = mqlAnalyzer.getTablePrimaryKey(realTableName);

		SqlBuilder sqlBuilder = new SqlBuilder(DELETE);
		sqlBuilder.append(FROM).append(realTableName).append(WHERE)
				.append(mainTablePk);
		generateDelConditon(deletePks, sqlBuilder);

		return sqlBuilder;
	}

	private void generateDelConditon(List<Object> deletePks,
			SqlBuilder sqlBuilder) {
		if (deletePks.size() > 20) {
			sqlBuilder.append(EQUAL).append(QUESTION).setBatch(true);
		} else {
			sqlBuilder.append(IN).append(LEFT_BRACKET);
			for (int i = 0; i < deletePks.size(); i++) {
				sqlBuilder.append(QUESTION).append(COMMA);
			}
			sqlBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
		}
		sqlBuilder.addParameters(deletePks);
	}

	private SqlBuilder generateExtDeleteSql(CorpTable corpTable,
			List<Object> deletePks) {

		String extTableName = corpTable.getExtTableName();
		String extPkName = corpTable.getExtPkName();
		SqlBuilder sqlBuilder = new SqlBuilder(DELETE);
		sqlBuilder.append(FROM).append(extTableName).append(WHERE)
				.append(extPkName);
		generateDelConditon(deletePks, sqlBuilder);

		return sqlBuilder;
	}

	private SqlBuilder generateSelectSql() {

		SqlNode rootNode = sqlGrammar.getRootNode();
		SqlMetadata sqlMetadata = sqlGrammar.getSqlMetadata();
		SqlTable sqlTable = sqlMetadata.getUniqueTable();

		String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
		String tableAlias = sqlTable.getTableAlias();
		String mainTablePk = mqlAnalyzer.getTablePrimaryKey(realTableName);

		mqlNotify.setTableName(realTableName);
		mqlNotify.setPrimaryKeyColumn(mainTablePk);
		mqlNotify.setMqlOpertation(MqlOperation.DELETE);

		SqlBuilder sqlBuilder = new SqlBuilder(SELECT);
		sqlBuilder.append(tableAlias).disableSlipt().append(DOT)
				.append(mainTablePk).enableSlipt().append(FROM)
				.append(realTableName).append(tableAlias);

		SqlBuilder sqlTableExtBuilder = processSqlTableExt(sqlTable,
				realTableName);
		sqlBuilder.append(sqlTableExtBuilder);

		int childrenCount = rootNode.getChildrenCount();
		if (childrenCount <= 0) {
			return sqlBuilder;
		}

		SqlNode whereNode = rootNode.getChild(0);
		MqlConditionParser conditionParser = new MqlConditionParser(whereNode,
				parameterList, parameterMap);
		conditionParser.setMqlAnalyzer(mqlAnalyzer);

		SqlBuilder parseCondition = conditionParser.parse();
		sqlBuilder.append(WHERE).append(parseCondition);

		return sqlBuilder;
	}
}
