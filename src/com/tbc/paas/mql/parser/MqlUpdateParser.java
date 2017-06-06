package com.tbc.paas.mql.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlMetadata;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.SqlPhase;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.notify.MqlNotify;
import com.tbc.paas.mql.parser.attach.MqlUpdateAttach;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlBuilder;
import static com.tbc.paas.mql.util.SqlConstants.*;

public class MqlUpdateParser extends MqlParser {

	private MqlUpdateAttach mqlUpdateAttach;

	public MqlUpdateParser() {
		super();
	}

	public MqlUpdateParser(SqlGrammar sqlGrammar, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterList, parameterMap);
	}

	public MqlUpdateParser(SqlGrammar sqlGrammar, List<Object> parameterList) {
		super(sqlGrammar, parameterList);
	}

	public MqlUpdateParser(SqlGrammar sqlGrammar,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterMap);
	}

	public MqlUpdateParser(SqlGrammar sqlGrammar) {
		super(sqlGrammar);
	}

	@Override
	public List<SqlBuilder> parse() {
		mqlAnalyzer.analyze();
		SqlBuilder sqlSelectBuilder = generateSelectSql();

		List<SqlBuilder> sqls = new ArrayList<SqlBuilder>();
		List<Object> updatePks = executePkQuery(sqlSelectBuilder);
		if (updatePks == null || updatePks.isEmpty()) {
			return sqls;
		}

		mqlNotify.setAffectedPrimaryKeyValueList(updatePks);

		List<SqlBuilder> updateSqls = generateUpdateSql(updatePks);

		sqls.addAll(updateSqls);

		return sqls;
	}

	private List<SqlBuilder> generateUpdateSql(List<Object> updatePks) {

		Map<String, SqlBuilder> updateSqlMap = new HashMap<String, SqlBuilder>();
		SqlNode rootNode = this.sqlGrammar.getRootNode();
		SqlNode columnNodes = rootNode.getChild(0);
		int columnChildrenCount = columnNodes.getChildrenCount();
		for (int i = 0; i < columnChildrenCount; i++) {
			SqlNode columnNode = columnNodes.getChild(i);
			SqlNode column = columnNode.getChild(0);
			SqlColumn sqlColumn = (SqlColumn) column.getValue();

			SqlTable belongToSqlTable = mqlAnalyzer
					.getBelongToSqlTable(sqlColumn);
			String realTableName = belongToSqlTable.getTableName();
			if (!belongToSqlTable.isExtTable()) {
				realTableName = mqlAnalyzer.getRealTableName(belongToSqlTable);
			}
			String realColumneName = mqlAnalyzer.getRealColumnName(sqlColumn);

			SqlBuilder sqlBuilder = updateSqlMap.get(realTableName);
			if (sqlBuilder == null) {
				sqlBuilder = new SqlBuilder(UPDATE);
				sqlBuilder.append(realTableName).append(SET);
				updateSqlMap.put(realTableName, sqlBuilder);
			}
			sqlBuilder.append(realColumneName).append(EQUAL);
			processUpdateSegment(realTableName, realColumneName, columnNode,
					updateSqlMap);
		}

		List<SqlBuilder> updateSqls = processUpdateCondition(updatePks,
				updateSqlMap);

		return updateSqls;
	}

	private void processUpdateSegment(String realTableName,
			String realColumnName, SqlNode columnNode,
			Map<String, SqlBuilder> updateSqlMap) {

		SqlBuilder sqlBuilder = updateSqlMap.get(realTableName);
		int size = columnNode.getChildrenCount();
		Object value = null;

		for (int i = 1; i < size; i++) {
			SqlNode child = columnNode.getChild(i);
			int childId = child.getId();
			switch (childId) {
			case SqlGrammarTreeConstants.JJTARITHMETICOPERATOR:
				String arithmeticOperator = child.getValue().toString();
				sqlBuilder.append(arithmeticOperator);
				break;
			case SqlGrammarTreeConstants.JJTPARAMETER:
			case SqlGrammarTreeConstants.JJTPLACEHOLDER:
			case SqlGrammarTreeConstants.JJTVALUE:
			case SqlGrammarTreeConstants.JJTNUMBER:
			case SqlGrammarTreeConstants.JJTBOOLEAN:
				MqlValueParser valueParser = new MqlValueParser(sqlBuilder,
						parameterList, parameterMap);
				valueParser.processValuePositionNode(child);
				value = valueParser.getValue();
				break;
			case SqlGrammarTreeConstants.JJTCOLUMNNAME:
				SqlNode column = columnNode.getChild(0);
				SqlColumn sqlColumn = (SqlColumn) column.getValue();
				processValueSqlColumn(realTableName, sqlColumn, updateSqlMap);
				break;
			default:
				throw new MdlException("Meet unexpected token "
						+ SqlGrammarTreeConstants.jjtNodeName[childId]);
			}
		}

		String tableName = mqlNotify.getTableName();
		if (tableName.equalsIgnoreCase(realTableName)) {
			if (size == 2) {
				mqlNotify.putAffectedColumn(realColumnName, value);
			} else {
				mqlNotify.putAffectedColumn(realColumnName,
						MqlNotify.COMPLEX_COLUMN_UPDATE);
			}
		}

		sqlBuilder.append(COMMA);
	}

	private void processValueSqlColumn(String mainTableName,
			SqlColumn sqlColumn, Map<String, SqlBuilder> updateSqlMap) {
		SqlTable belongToSqlTable = mqlAnalyzer.getBelongToSqlTable(sqlColumn);
		String realTableName = belongToSqlTable.getTableName();
		if (!belongToSqlTable.isExtTable()) {
			realTableName = mqlAnalyzer.getRealTableName(belongToSqlTable);
		}

		if (!realTableName.equalsIgnoreCase(mainTableName)) {
			throw new MqlParseException("Can't process "
					+ sqlColumn.getColumnName()
					+ " in UPDATE SET segment,because it's a ext column!");
		}

		String realColumneName = mqlAnalyzer.getRealColumnName(sqlColumn);
		SqlBuilder sqlBuilder = updateSqlMap.get(realTableName);
		sqlBuilder.append(realColumneName);
	}

	private List<SqlBuilder> processUpdateCondition(List<Object> updatePks,
			Map<String, SqlBuilder> updateSqlMap) {
		List<SqlBuilder> updateSqls = new ArrayList<SqlBuilder>();
		Set<Entry<String, SqlBuilder>> entrySet = updateSqlMap.entrySet();
		for (Entry<String, SqlBuilder> entry : entrySet) {
			String updateTableName = entry.getKey();
			SqlBuilder updateSqlBuilder = entry.getValue();

			if (mqlUpdateAttach != null) {
				SqlBuilder attachPart = mqlUpdateAttach
						.getColumnAttachPart(mqlAnalyzer);
				updateSqlBuilder.append(attachPart);
			}

			String primaryKey = mqlAnalyzer.getTablePrimaryKey(updateTableName);

			updateSqlBuilder.replaceOrAddLastSlice(WHERE, COMMA);
			if (updatePks.size() > 20) {
				updateSqlBuilder.append(primaryKey).append(EQUAL)
						.append(QUESTION).setBatch(true);
				List<Object> otherParameter = updateSqlBuilder
						.getParameterList();
				List<Object> parameters = new ArrayList<Object>();
				for (Object parameter : updatePks) {
					List<Object> rowParameter = new ArrayList<Object>();
					rowParameter.addAll(otherParameter);
					rowParameter.add(parameter);
					parameters.add(rowParameter);
				}
				updateSqlBuilder.setParameters(parameters);
			} else {
				updateSqlBuilder.append(primaryKey).append(IN)
						.append(LEFT_BRACKET);
				for (int i = 0; i < updatePks.size(); i++) {
					updateSqlBuilder.append(QUESTION).append(COMMA);
				}
				updateSqlBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
				updateSqlBuilder.addParameters(updatePks);
			}

			updateSqls.add(updateSqlBuilder);
		}
		return updateSqls;
	}

	private SqlBuilder generateSelectSql() {

		SqlBuilder sqlBuilder = processTablePart();

		SqlNode rootNode = this.sqlGrammar.getRootNode();
		if (rootNode.getChildrenCount() <= 1) {
			return sqlBuilder;
		}

		SqlNode whereNode = rootNode.getChild(1);
		MqlConditionParser conditionParser = new MqlConditionParser(whereNode,
				parameterList, parameterMap);
		conditionParser.setMqlAnalyzer(mqlAnalyzer);

		SqlBuilder parseCondition = conditionParser.parse();

		sqlBuilder.append(WHERE).append(parseCondition);

		return sqlBuilder;
	}

	private SqlBuilder processTablePart() {
		SqlMetadata sqlMetadata = this.sqlGrammar.getSqlMetadata();
		SqlTable mainTable = sqlMetadata.getUniqueTable();
		String realTableName = mqlAnalyzer.getRealTableName(mainTable);
		String tableAlias = mainTable.getTableAlias();
		String tablePk = mqlAnalyzer.getTablePrimaryKey(realTableName);

		mqlNotify.setTableName(realTableName);
		mqlNotify.setPrimaryKeyColumn(tablePk);
		mqlNotify.setMqlOpertation(MqlOperation.UPDATE);

		SqlBuilder sqlBuilder = new SqlBuilder(SELECT);
		sqlBuilder.append(tableAlias).disableSlipt().append(DOT)
				.append(tablePk).enableSlipt().append(FROM)
				.append(realTableName).append(tableAlias);

		Set<SqlTable> queryJoinTable = getExtTableForSelect(realTableName,
				sqlMetadata);

		for (SqlTable extSqlTable : queryJoinTable) {
			String extTableName = extSqlTable.getTableName();
			String extTableAlias = extSqlTable.getTableAlias();
			String extTablePk = mqlAnalyzer.getTablePrimaryKey(extTableName);

			sqlBuilder.append(LEFT_JOIN).append(extTableName)
					.append(extTableAlias).append(ON).append(tableAlias)
					.disableSlipt().append(DOT).append(tablePk).enableSlipt()
					.append(EQUAL).append(extTableAlias).disableSlipt()
					.append(DOT).append(extTablePk).enableSlipt();
		}

		return sqlBuilder;
	}

	private Set<SqlTable> getExtTableForSelect(String realTableName,
			SqlMetadata sqlMetadata) {
		Set<SqlTable> queryJoinTable = new HashSet<SqlTable>();
		List<SqlColumn> columnList = sqlMetadata.getColumnList(SqlPhase.WHERE);
		if (columnList == null) {
			return queryJoinTable;
		}

		Map<SqlColumn, SqlTable> sqlColumnTableMap = mqlAnalyzer
				.getSqlColumnTableMap();
		for (SqlColumn sqlColumn : columnList) {
			SqlTable sqlTable = sqlColumnTableMap.get(sqlColumn);
			String extTableName = sqlTable.getTableName();
			if (sqlTable.isExtTable()
					&& !extTableName.equalsIgnoreCase(realTableName)) {
				queryJoinTable.add(sqlTable);
			}
		}

		return queryJoinTable;
	}

	public MqlUpdateAttach getMqlUpdateAttach() {
		return mqlUpdateAttach;
	}

	public void setMqlUpdateAttach(MqlUpdateAttach mqlUpdateAttach) {
		this.mqlUpdateAttach = mqlUpdateAttach;
	}
}
