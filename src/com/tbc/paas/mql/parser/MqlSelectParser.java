package com.tbc.paas.mql.parser;

import static com.tbc.paas.mql.util.SqlConstants.BY;
import static com.tbc.paas.mql.util.SqlConstants.COMMA;
import static com.tbc.paas.mql.util.SqlConstants.DISTINCT;
import static com.tbc.paas.mql.util.SqlConstants.FROM;
import static com.tbc.paas.mql.util.SqlConstants.GROUP;
import static com.tbc.paas.mql.util.SqlConstants.HAVING;
import static com.tbc.paas.mql.util.SqlConstants.LEFT_BRACKET;
import static com.tbc.paas.mql.util.SqlConstants.LIMIT;
import static com.tbc.paas.mql.util.SqlConstants.OFFSET;
import static com.tbc.paas.mql.util.SqlConstants.ON;
import static com.tbc.paas.mql.util.SqlConstants.ORDER;
import static com.tbc.paas.mql.util.SqlConstants.RIGHT_BRACKET;
import static com.tbc.paas.mql.util.SqlConstants.SELECT;
import static com.tbc.paas.mql.util.SqlConstants.WHERE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.SqlResultColumn;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.parser.attach.MqlSelectAttach;
import com.tbc.paas.mql.parser.dialect.OracleDialect;
import com.tbc.paas.mql.util.SqlAliasGenerator;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;

public class MqlSelectParser extends MqlParser {

	// private boolean encapsulatable;
	private MqlSelectAttach selectAttach;
	private List<SqlResultColumn> resultColumnList;

	public MqlSelectParser() {
		super();
		init();
	}

	public MqlSelectParser(SqlGrammar sqlGrammar, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterList, parameterMap);
		init();
	}

	public MqlSelectParser(SqlGrammar sqlGrammar, List<Object> parameterList) {
		super(sqlGrammar, parameterList);
		init();
	}

	public MqlSelectParser(SqlGrammar sqlGrammar,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterMap);
		init();
	}

	public MqlSelectParser(SqlGrammar sqlGrammar) {
		super(sqlGrammar);
		init();
	}

	private void init() {
		// oracle = false;
		// encapsulatable = true;
		resultColumnList = new ArrayList<SqlResultColumn>();
	}

	@Override
	public List<SqlBuilder> parse() {
		mqlAnalyzer.analyze();
		SqlNode rootNode = this.sqlGrammar.getRootNode();
		SqlBuilder mdlBuilder = new SqlBuilder(SELECT);

		int childrenCount = rootNode.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {
			SqlNode child = rootNode.getChild(i);
			int childId = child.getId();
			switch (childId) {
			case SqlGrammarTreeConstants.JJTCOLUMNSEGMENT:
				SqlBuilder columnSegmentSql = processColumnSegment(child);
				mdlBuilder.append(columnSegmentSql);
				break;
			case SqlGrammarTreeConstants.JJTFROMSEGMENT:
				SqlBuilder fromSegmentSql = processFromSegment(child);
				mdlBuilder.append(fromSegmentSql);
				break;
			case SqlGrammarTreeConstants.JJTWHERESEGMENT:
				SqlBuilder whereSegmentSql = processWhereSegment(child);
				mdlBuilder.append(whereSegmentSql);
				break;
			case SqlGrammarTreeConstants.JJTGROUPSEGMENT:
				// this.encapsulatable = false;
				SqlBuilder groupSegmentSql = processGroupSegment(child);
				mdlBuilder.append(groupSegmentSql);
				break;
			case SqlGrammarTreeConstants.JJTORDERSEGMENT:
				SqlBuilder orderSegmentSql = processOrderSegment(child);
				mdlBuilder.append(orderSegmentSql);
				break;
			case SqlGrammarTreeConstants.JJTLIMITSEGMENT:
				if (oracle) {
					OracleDialect oracleDialect = new OracleDialect();
					mdlBuilder = oracleDialect.processLimitSegment(mdlBuilder,
							child, parameterList, parameterMap);
				} else {
					SqlBuilder limitSetmentSql = processLimitSegment(child);
					mdlBuilder.append(limitSetmentSql);
				}
				break;
			default:
				throw new MqlParseException("Can't  process sql node  ("
						+ SqlGrammarTreeConstants.jjtNodeName[childId] + ")");
			}
		}

		List<SqlBuilder> sqls = new ArrayList<SqlBuilder>();
		sqls.add(mdlBuilder);

		return sqls;
	}

	protected SqlBuilder processMdlConditionNode(SqlNode conditionNode) {
		MqlConditionParser conditionParser = new MqlConditionParser(
				conditionNode, parameterList, parameterMap);
		conditionParser.setMqlAnalyzer(mqlAnalyzer);
		SqlBuilder conditonBuilder = conditionParser.parse();
		return conditonBuilder;
	}

	protected SqlBuilder processLimitSegment(SqlNode limitSegment) {
		SqlBuilder limitSegmentSql = new SqlBuilder();
		int childrenCount = limitSegment.getChildrenCount();

		for (int i = 0; i < childrenCount; i++) {
			SqlNode childNode = limitSegment.getChild(i);
			int childId = childNode.getId();
			if (childId == SqlGrammarTreeConstants.JJTLIMIT) {
				limitSegmentSql.append(LIMIT);
				SqlNode limitValueNode = childNode.getChild(0);
				MqlValueParser valueNodeParser = new MqlValueParser(
						limitSegmentSql, parameterList, parameterMap);
				valueNodeParser.processValuePositionNode(limitValueNode);
			} else if (childId == SqlGrammarTreeConstants.JJTOFFSET) {
				limitSegmentSql.append(OFFSET);
				SqlNode offsetValueNode = childNode.getChild(0);
				MqlValueParser valueNodeParser = new MqlValueParser(
						limitSegmentSql, parameterList, parameterMap);
				valueNodeParser.processValuePositionNode(offsetValueNode);
			}
		}

		return limitSegmentSql;
	}

	protected SqlBuilder processOrderSegment(SqlNode orderSegment) {
		SqlBuilder orderBuilder = new SqlBuilder(ORDER).append(BY);

		int childrenCount = orderSegment.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {
			SqlNode child = orderSegment.getChild(i);
			int childId = child.getId();
			if (childId == SqlGrammarTreeConstants.JJTCOLUMNNAME) {
				processSqlColumnNode(child, orderBuilder);
			} else if (childId == SqlGrammarTreeConstants.JJTORDER) {
				orderBuilder.removeLastSlice(COMMA);
				String order = child.getValue().toString();
				orderBuilder.append(order).append(COMMA);
			}else if(childId == SqlGrammarTreeConstants.JJTAGGREGATE){
				processAggregateFunction(orderBuilder, child);
			}
		}

		orderBuilder.removeLastSlice(COMMA);

		return orderBuilder;
	}

	protected SqlBuilder processGroupSegment(SqlNode groupNode) {
		SqlBuilder groupSqlBuilder = new SqlBuilder(GROUP);
		groupSqlBuilder.append(BY);

		int childrenCount = groupNode.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {
			SqlNode child = groupNode.getChild(i);
			int childId = child.getId();
			if (childId == SqlGrammarTreeConstants.JJTCOLUMNNAME) {
				processSqlColumnNode(child, groupSqlBuilder);
			} else if (childId == SqlGrammarTreeConstants.JJTHAVING) {
				groupSqlBuilder.replaceOrAddLastSlice(HAVING, COMMA);
				SqlBuilder havingCondition = processMdlConditionNode(child);
				groupSqlBuilder.append(havingCondition);
			}
		}

		groupSqlBuilder.removeLastSlice(COMMA);

		return groupSqlBuilder;
	}

	protected SqlBuilder processWhereSegment(SqlNode whereNode) {
		SqlBuilder whereSqlBuilder = new SqlBuilder(WHERE);
		SqlBuilder conditionBuilder = processMdlConditionNode(whereNode);
		whereSqlBuilder.append(conditionBuilder);
		return whereSqlBuilder;
	}

	protected SqlBuilder processFromSegment(SqlNode fromSegment) {
		SqlBuilder fromSegmentBuilder = new SqlBuilder(FROM);
		int tableCount = fromSegment.getChildrenCount();

		for (int i = 0; i < tableCount; i++) {
			SqlNode tableNode = fromSegment.getChild(i);
			SqlBuilder tableSqlBuilder = processSqlTableNode(tableNode);
			fromSegmentBuilder.append(tableSqlBuilder);
			fromSegmentBuilder.append(COMMA);
		}

		fromSegmentBuilder.removeLastSlice(COMMA);
		return fromSegmentBuilder;
	}

	protected SqlBuilder processSqlTableNode(SqlNode tableNode) {
		SqlTable sqlTable = (SqlTable) tableNode.getValue();
		String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
		String tableAlias = sqlTable.getTableAlias();

		SqlBuilder tableBuilder = new SqlBuilder();
		tableBuilder.append(realTableName).append(tableAlias);

		SqlBuilder extTableJoin = processSqlTableExt(sqlTable, realTableName);
		tableBuilder.append(extTableJoin);

		int childrenCount = tableNode.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {
			SqlNode joinNode = tableNode.getChild(i);
			SqlBuilder processJoinSegment = processJoinSegment(joinNode);
			tableBuilder.append(processJoinSegment);
		}

		return tableBuilder;
	}

	protected SqlBuilder processJoinSegment(SqlNode joinNode) {
		SqlBuilder joinSegmentBuilder = new SqlBuilder();

		String joinType = joinNode.getValue().toString();
		joinSegmentBuilder.append(joinType);

		SqlNode joinTableNode = joinNode.getChild(0);
		SqlTable sqlTable = (SqlTable) joinTableNode.getValue();
		String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
		String tableAlias = sqlTable.getTableAlias();
		joinSegmentBuilder.append(realTableName).append(tableAlias).append(ON);

		SqlNode blockConditionslice = joinNode.getChild(1);
		MqlConditionParser conditionParser = new MqlConditionParser(
				blockConditionslice, parameterList, parameterMap);
		conditionParser.setMqlAnalyzer(mqlAnalyzer);
		SqlBuilder joinConditionBuilder = conditionParser.parse();
		joinSegmentBuilder.append(joinConditionBuilder);

		SqlBuilder extTableJoinBuilder = processSqlTableExt(sqlTable,
				realTableName);
		joinSegmentBuilder.append(extTableJoinBuilder);

		return joinSegmentBuilder;
	}

	protected SqlBuilder processColumnSegment(SqlNode columnSegment) {

		SqlBuilder columnSegmentBuilder = null;
		Map<SqlColumn, SqlResultColumn> sqlColumnResultMap = mqlAnalyzer
				.getSqlColumnResultMap();
		columnSegmentBuilder = processColumns(columnSegment, sqlColumnResultMap);

		processRelColumns(columnSegmentBuilder, sqlColumnResultMap);

		if (selectAttach != null) {
			SqlBuilder selectColumnAttach = selectAttach.getColumnAttach(this);
			columnSegmentBuilder.append(selectColumnAttach);
		}

		columnSegmentBuilder.removeLastSlice(COMMA);
		return columnSegmentBuilder;
	}

	protected void processRelColumns(SqlBuilder columnSegmentBuilder,
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap) {
		List<SqlColumn> sqlTableRelColumnList = mqlAnalyzer
				.getSqlTableRelColumnList();
		if (sqlTableRelColumnList != null && sqlTableRelColumnList.size() > 0) {
			columnSegmentBuilder.append(COMMA);
		}
		for (SqlColumn sqlColumn : sqlTableRelColumnList) {
			processSqlColumn(sqlColumn, columnSegmentBuilder,
					sqlColumnResultMap, null);
			columnSegmentBuilder.append(COMMA);
		}
	}

	protected SqlBuilder processColumns(SqlNode columnSegment,
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap) {

		SqlBuilder columnSegmentBuilder = new SqlBuilder();
		int columnCount = columnSegment.getChildrenCount();
		for (int i = 0; i < columnCount; i++) {
			SqlNode column = columnSegment.getChild(i);
			int columnId = column.getId();
			switch (columnId) {
			case SqlGrammarTreeConstants.JJTCOLUMN:

				String columnAlias = null;
				int childrenCount = column.getChildrenCount();
				if (childrenCount > 1) {
					SqlNode secondChild = column.getChild(1);
					columnAlias = secondChild.getValue().toString();
				}

				processSelectColumnNode(sqlColumnResultMap,
						columnSegmentBuilder, column, columnAlias);

				columnSegmentBuilder.append(COMMA);
				break;
			case SqlGrammarTreeConstants.JJTDISTINCT:
				columnSegmentBuilder.append(DISTINCT);
				break;
			default:
				throw new MdlException("Can't support token "
						+ SqlGrammarTreeConstants.jjtNodeName[columnId]);
			}
		}

		columnSegmentBuilder.removeLastSlice(COMMA);
		return columnSegmentBuilder;
	}

	protected void processSelectColumnNode(
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap,
			SqlBuilder columnSegmentBuilder, SqlNode column, String columnAlias) {
		SqlNode firstChild = column.getChild(0);
		int childId = firstChild.getId();
		if (childId == SqlGrammarTreeConstants.JJTCOLUMNNAME) {
			processSqlColumnSlice(sqlColumnResultMap, columnSegmentBuilder,
					firstChild, columnAlias);
		} else if (childId == SqlGrammarTreeConstants.JJTAGGREGATE) {
			// this.encapsulatable = false;
			String aggregateFunction = firstChild.getValue().toString();
			SqlResultColumn sqlResultColumn=processAggregateFunction(columnSegmentBuilder, firstChild);
			if (columnAlias == null) {
				SqlAliasGenerator aliasGenerator = mqlAnalyzer
						.getAliasGenerator();
				columnAlias = aliasGenerator.generate(aggregateFunction);
			}
			columnSegmentBuilder.append(columnAlias);

			sqlResultColumn.setAggregation(true);
			resultColumnList.add(sqlResultColumn);
		}
	}

	private SqlResultColumn processAggregateFunction(SqlBuilder columnSegmentBuilder,
			SqlNode aggregateFunctionNode) {
		String aggregateFunction = aggregateFunctionNode.getValue().toString();
		columnSegmentBuilder.append(aggregateFunction);
		columnSegmentBuilder.append(LEFT_BRACKET);
		SqlNode columnNode = aggregateFunctionNode.getChild(0);
		int aggregateColumnId = columnNode.getId();

		SqlResultColumn sqlResultColumn = new SqlResultColumn();
		sqlResultColumn.setColumnName(aggregateFunction);
		
		if (aggregateColumnId == SqlGrammarTreeConstants.JJTCOLUMNNAME) {
			SqlColumn sqlColumn = (SqlColumn) columnNode.getValue();
			String actualColumnName = sqlColumn.getActualColumnName();
			if (SqlConstants.ASTERISK.equals(actualColumnName)) {
				columnSegmentBuilder.append(SqlConstants.ASTERISK);
				sqlResultColumn.setColumnAlias(SqlConstants.ASTERISK);
			} else {
				SqlTable sqlTable = mqlAnalyzer.getSqlColumnTableMap().get(sqlColumn);
				String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
				String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
				sqlResultColumn.setTableName(realTableName);
				sqlResultColumn.setColumnAlias(sqlColumnName);
				columnSegmentBuilder.append(sqlColumnName);
			}
		} else if (aggregateColumnId == SqlGrammarTreeConstants.JJTFUNCTIONDISTINCT) {
			columnSegmentBuilder.append(DISTINCT);
			SqlNode child = columnNode.getChild(0);
			SqlColumn sqlColumn = (SqlColumn) child.getValue();
			
			SqlTable sqlTable = mqlAnalyzer.getSqlColumnTableMap().get(sqlColumn);
			String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
			String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
			sqlResultColumn.setTableName(realTableName);
			sqlResultColumn.setColumnAlias(sqlColumnName);
			columnSegmentBuilder.append(sqlColumnName);
		}

		columnSegmentBuilder.append(RIGHT_BRACKET);
		return sqlResultColumn;
	}

	protected void processSqlColumnSlice(
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap,
			SqlBuilder columnSegmentBuilder, SqlNode firstChild,
			String columnAlias) {
		SqlColumn sqlColumn = (SqlColumn) firstChild.getValue();
		String actualColumnName = sqlColumn.getActualColumnName();
		if (!actualColumnName.equals(SqlConstants.ASTERISK)) {
			processSqlColumn(sqlColumn, columnSegmentBuilder,
					sqlColumnResultMap, columnAlias);
			return;
		}

		List<SqlColumn> sqlAsteriskColumns = mqlAnalyzer
				.getSqlAsteriskColumns(sqlColumn);
		for (SqlColumn realSqlColumn : sqlAsteriskColumns) {
			processSqlColumn(realSqlColumn, columnSegmentBuilder,
					sqlColumnResultMap, null);
			columnSegmentBuilder.append(COMMA);
		}
		columnSegmentBuilder.removeLastSlice(COMMA);
	}

	protected void processSqlColumn(SqlColumn sqlColumn,
			SqlBuilder columnSegmentBuilder,
			Map<SqlColumn, SqlResultColumn> sqlColumnResultMap,
			String columnAlias) {
		String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
		if (columnAlias == null) {
			SqlAliasGenerator aliasGenerator = mqlAnalyzer.getAliasGenerator();
			columnAlias = aliasGenerator.generate(sqlColumnName);
		}
		columnSegmentBuilder.append(sqlColumnName).append(columnAlias);
		SqlResultColumn sqlResultColumn = sqlColumnResultMap.get(sqlColumn);
		resultColumnList.add(sqlResultColumn);
	}

	protected void processSqlColumnNode(SqlNode child, SqlBuilder orderBuilder) {
		SqlColumn sqlColumn = (SqlColumn) child.getValue();
		String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
		orderBuilder.append(sqlColumnName).append(COMMA);
	}

	// public boolean isEncapsulatable() {
	// return this.encapsulatable;
	// }

	public List<SqlResultColumn> getResultColumnList() {
		return resultColumnList;
	}

	public void setResultColumnList(List<SqlResultColumn> resultColumnList) {
		this.resultColumnList = resultColumnList;
	}

	public MqlSelectAttach getSelectAttach() {
		return selectAttach;
	}

	public void setSelectAttach(MqlSelectAttach selectAttach) {
		this.selectAttach = selectAttach;
	}
}
