package com.tbc.paas.mql.parser;

import static com.tbc.paas.mql.util.SqlConstants.AND;
import static com.tbc.paas.mql.util.SqlConstants.BETWEEN;
import static com.tbc.paas.mql.util.SqlConstants.COMMA;
import static com.tbc.paas.mql.util.SqlConstants.DISTINCT;
import static com.tbc.paas.mql.util.SqlConstants.ILIKE;
import static com.tbc.paas.mql.util.SqlConstants.IN;
import static com.tbc.paas.mql.util.SqlConstants.IS;
import static com.tbc.paas.mql.util.SqlConstants.LEFT_BRACKET;
import static com.tbc.paas.mql.util.SqlConstants.LIKE;
import static com.tbc.paas.mql.util.SqlConstants.NOT;
import static com.tbc.paas.mql.util.SqlConstants.NULL;
import static com.tbc.paas.mql.util.SqlConstants.OR;
import static com.tbc.paas.mql.util.SqlConstants.RIGHT_BRACKET;

import java.util.List;
import java.util.Map;

import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;

public class MqlConditionParser {
	private SqlBuilder sqlCondition;
	private SqlNode conditionNode;
	private MqlAnalyzer mqlAnalyzer;

	private List<Object> parameterList;
	private Map<String, Object> parameterMap;

	public MqlConditionParser() {
		this(null, null, null);
	}

	public MqlConditionParser(SqlNode conditionNode) {
		this(conditionNode, null, null);
	}

	public MqlConditionParser(SqlNode conditionNode, List<Object> parameters) {
		this(conditionNode, parameters, null);
	}

	public MqlConditionParser(SqlNode conditionNode,
			Map<String, Object> parameterMap) {
		this(conditionNode, null, parameterMap);
	}

	public MqlConditionParser(SqlNode conditionNode,
			List<Object> parameterList, Map<String, Object> parameterMap) {
		super();
		this.sqlCondition = new SqlBuilder();
		this.conditionNode = conditionNode;
		this.parameterList = parameterList;
		this.parameterMap = parameterMap;
	}

	public SqlBuilder parse() {

		if (conditionNode != null) {
			processBlockConditionSlice(conditionNode);
		}

		return sqlCondition;
	}

	private void processValuePositionNode(SqlNode valueNode) {
		MqlValueParser valueParser = new MqlValueParser(sqlCondition,
				parameterList, parameterMap);
		valueParser.processValuePositionNode(valueNode);
	}

	protected void processColumnNameNode(SqlNode child) {
		SqlColumn sqlColumn = (SqlColumn) child.getValue();
		String columnBuilder = mqlAnalyzer.getRealColumnName(sqlColumn);
		sqlCondition.append(columnBuilder);
	}

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

	public SqlBuilder getSqlCondition() {
		return sqlCondition;
	}

	protected void processBlockConditionSlice(SqlNode blockConditionslice) {

		int childrenCount = blockConditionslice.getChildrenCount();

		for (int i = 0; i < childrenCount; i++) {
			SqlNode child = blockConditionslice.getChild(i);
			int nodeId = child.getId();

			switch (nodeId) {
			case SqlGrammarTreeConstants.JJTBLOCKCONDITION:
				sqlCondition.append(LEFT_BRACKET);
				processBlockConditionSlice(child);
				sqlCondition.append(RIGHT_BRACKET);
				break;
			case SqlGrammarTreeConstants.JJTCONDITIONSLICE:
				processConditionSlice(child);
				break;
			case SqlGrammarTreeConstants.JJTADD:
				sqlCondition.append(AND);
				break;
			case SqlGrammarTreeConstants.JJTOR:
				sqlCondition.append(OR);
				break;
			default:
				throw new MqlParseException(
						"Can't process sql condition slice! "
								+ SqlGrammarTreeConstants.jjtNodeName[nodeId]);
			}
		}

	}

	protected void processConstantConditionSlice(SqlNode conditionslice) {
		int childrenCount = conditionslice.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {
			SqlNode child = conditionslice.getChild(i);
			String value = child.getValue().toString();
			sqlCondition.append(value);
		}
	}

	protected void processsNormalConditionSlice(SqlNode conditionslice) {
		int childrenCount = conditionslice.getChildrenCount();
		for (int i = 0; i < childrenCount; i++) {

			SqlNode child = conditionslice.getChild(i);
			int nodeId = child.getId();
			switch (nodeId) {
			case SqlGrammarTreeConstants.JJTCOLUMNNAME:
				processColumnNameNode(child);
				break;
			case SqlGrammarTreeConstants.JJTBINARYOPERATOR:
				String binaryOperator = child.getValue().toString();
				sqlCondition.append(binaryOperator);
				break;
			case SqlGrammarTreeConstants.JJTNOT:
				sqlCondition.append(NOT);
				break;
			case SqlGrammarTreeConstants.JJTIN: 
				processSqlInNode(child);
				break;
			case SqlGrammarTreeConstants.JJTLIKE:
				processSqlLikeNode(child);
				break;
			case SqlGrammarTreeConstants.JJTNULL:
				processSqlNullNode(child);
				break;
			case SqlGrammarTreeConstants.JJTBETWEEN:
				processSqlBetweenNode(child);
				break;
			case SqlGrammarTreeConstants.JJTPARAMETER:
			case SqlGrammarTreeConstants.JJTPLACEHOLDER:
			case SqlGrammarTreeConstants.JJTVALUE:
			case SqlGrammarTreeConstants.JJTNUMBER:
			case SqlGrammarTreeConstants.JJTBOOLEAN:
				processValuePositionNode(child);
				break;
			case SqlGrammarTreeConstants.JJTAGGREGATE:
				processAggregateFunction(child);
				break;
			default:
				throw new UnsupportedOperationException(
						"Can't process sql condition slice! "
								+ SqlGrammarTreeConstants.jjtNodeName[nodeId]);
			}
		}
	}
	
	private void processAggregateFunction(SqlNode aggregateFunctionNode) {
		String aggregateFunction = aggregateFunctionNode.getValue().toString();
		sqlCondition.append(aggregateFunction);
		sqlCondition.append(LEFT_BRACKET);
		SqlNode columnNode = aggregateFunctionNode.getChild(0);
		int aggregateColumnId = columnNode.getId();

		
		if (aggregateColumnId == SqlGrammarTreeConstants.JJTCOLUMNNAME) {
			SqlColumn sqlColumn = (SqlColumn) columnNode.getValue();
			String actualColumnName = sqlColumn.getActualColumnName();
			if (SqlConstants.ASTERISK.equals(actualColumnName)) {
				sqlCondition.append(SqlConstants.ASTERISK);
			} else {
				String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
				sqlCondition.append(sqlColumnName);
			}
		} else if (aggregateColumnId == SqlGrammarTreeConstants.JJTFUNCTIONDISTINCT) {
			sqlCondition.append(DISTINCT);
			SqlNode child = columnNode.getChild(0);
			SqlColumn sqlColumn = (SqlColumn) child.getValue();
			String sqlColumnName = mqlAnalyzer.getRealColumnName(sqlColumn);
			sqlCondition.append(sqlColumnName);
		}

		sqlCondition.append(RIGHT_BRACKET);
	}

	protected void processConditionSlice(SqlNode conditionslice) {
		int childrenCount = conditionslice.getChildrenCount();
		if (childrenCount != 3) {
			processsNormalConditionSlice(conditionslice);
			return;
		}

		int firstNodeId = conditionslice.getChild(0).getId();
		int secondNodeId = conditionslice.getChild(2).getId();
		if ((firstNodeId == SqlGrammarTreeConstants.JJTNUMBER || firstNodeId == SqlGrammarTreeConstants.JJTVALUE)
				&& (secondNodeId == SqlGrammarTreeConstants.JJTNUMBER || secondNodeId == SqlGrammarTreeConstants.JJTVALUE)) {
			processConstantConditionSlice(conditionslice);
		} else {
			processsNormalConditionSlice(conditionslice);
		}
	}

	protected void processSqlInNode(SqlNode sqlInNode) {
		sqlCondition.append(IN).append(LEFT_BRACKET);

		for (int j = 0; j < sqlInNode.getChildrenCount(); j++) {
			SqlNode valueNode = sqlInNode.getChild(j);
			processValuePositionNode(valueNode);
			sqlCondition.append(COMMA);
		}

		sqlCondition.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
	}

	protected void processSqlLikeNode(SqlNode sqlLikeNode) {
		if (MqlParser.isOracle()) {
			sqlCondition.append(LIKE);
		} else {
			sqlCondition.append(ILIKE);
		}
		SqlNode valueNode = sqlLikeNode.getChild(0);

		processValuePositionNode(valueNode);
	}

	protected void processSqlNullNode(SqlNode sqlNullNode) {
		sqlCondition.append(IS);
		Object obj = sqlNullNode.getValue();
		if (obj != null) {
			sqlCondition.append(NOT);
		}

		sqlCondition.append(NULL);
	}

	protected void processSqlBetweenNode(SqlNode betweenNode) {
		sqlCondition.append(BETWEEN);
		SqlNode firstParameter = betweenNode.getChild(0);
		processValuePositionNode(firstParameter);

		sqlCondition.append(AND);
		SqlNode secondParameter = betweenNode.getChild(1);
		processValuePositionNode(secondParameter);
	}

	public MqlAnalyzer getMqlAnalyzer() {
		return mqlAnalyzer;
	}

	public void setMqlAnalyzer(MqlAnalyzer mqlAnalyzer) {
		this.mqlAnalyzer = mqlAnalyzer;
	}
}
