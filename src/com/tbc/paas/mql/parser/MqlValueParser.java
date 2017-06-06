package com.tbc.paas.mql.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.MqlParseException;
import com.tbc.paas.mql.grammar.SqlGrammarTreeConstants;
import com.tbc.paas.mql.util.SqlBuilder;
import com.tbc.paas.mql.util.SqlConstants;

public class MqlValueParser {

	private Object parameter;
	private SqlBuilder sqlCondition;

	private List<Object> parameterList;
	private Map<String, Object> parameterMap;

	public MqlValueParser() {
		this(null, null, null);
	}

	public MqlValueParser(SqlBuilder sqlCondition) {
		this(sqlCondition, null, null);
	}

	public MqlValueParser(SqlBuilder sqlCondition,
			Map<String, Object> parameterMap) {
		this(sqlCondition, null, parameterMap);
	}

	public MqlValueParser(SqlBuilder sqlCondition, List<Object> parameterList) {
		this(sqlCondition, parameterList, null);
	}

	public MqlValueParser(SqlBuilder sqlCondition, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super();
		this.sqlCondition = sqlCondition;
		this.parameterList = parameterList;
		this.parameterMap = parameterMap;
	}

	public SqlBuilder getSqlCondition() {
		return sqlCondition;
	}

	public void setSqlCondition(SqlBuilder sqlCondition) {
		this.sqlCondition = sqlCondition;
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

	public void processValuePositionNode(SqlNode valueNode) {
		int nodeId = valueNode.getId();
		switch (nodeId) {
		case SqlGrammarTreeConstants.JJTPARAMETER:
			processParameterNode(valueNode);
			break;
		case SqlGrammarTreeConstants.JJTPLACEHOLDER:
			processPlaceholderNode(valueNode);
			break;
		case SqlGrammarTreeConstants.JJTVALUE:
			processValueNode(valueNode);
			break;
		case SqlGrammarTreeConstants.JJTNUMBER:
			processNumberNode(valueNode);
			break;
		case SqlGrammarTreeConstants.JJTBOOLEAN:
			processBooleanNode(valueNode);
			break;
		default:
			throw new MqlParseException("Meet unexpected token "
					+ SqlGrammarTreeConstants.jjtNodeName[nodeId]);
		}
	}

	private void processBooleanNode(SqlNode valueNode) {
		sqlCondition.append(SqlConstants.QUESTION);
		String value = valueNode.getValue().toString();
		parameter = Boolean.parseBoolean(value);
		sqlCondition.addParameter(processDialetValue(parameter));
	}

	private void processNumberNode(SqlNode valueNode) {
		sqlCondition.append(SqlConstants.QUESTION);
		String value = valueNode.getValue().toString();
		try {
			if (value.indexOf(SqlConstants.DOT) == -1) {
				parameter = Integer.parseInt(value);
			} else {
				parameter = Double.parseDouble(value);
			}
		} catch (Exception e) {
			parameter = Double.parseDouble(value);
		}

		sqlCondition.addParameter(parameter);
	}

	private void processValueNode(SqlNode child) {
		sqlCondition.append(SqlConstants.QUESTION);
		String value = child.getValue().toString();
		parameter = value.substring(1, value.length() - 1);
		sqlCondition.addParameter(processDialetValue(parameter));
	}

	private void processPlaceholderNode(SqlNode child) {

		String placeHoldName = child.getValue().toString();
		if (this.parameterMap == null) {
			throw new MqlParseException(
					"parameters has not been setted for placeholder !");
		}

		parameter = this.parameterMap.get(placeHoldName);
		if (parameter instanceof Collection<?>) {
			Collection<?> paraCollection = (Collection<?>) parameter;
			for (Object para : paraCollection) {
				sqlCondition.append(SqlConstants.QUESTION).append(
						SqlConstants.COMMA);
				sqlCondition.addParameter(processDialetValue(para));
			}
			sqlCondition.removeLastSlice(SqlConstants.COMMA);

		} else {
			sqlCondition.append(SqlConstants.QUESTION);
			sqlCondition.addParameter(processDialetValue(parameter));
		}

	}

	private void processParameterNode(SqlNode sqlParameterNode) {
		sqlCondition.append(SqlConstants.QUESTION);
		if (parameterList == null) {
			throw new MqlParseException("parameters has not been setted !");
		}

		Integer index = (Integer) sqlParameterNode.getValue();
		parameter = parameterList.get(index);
		sqlCondition.addParameter(processDialetValue(parameter));
	}

	public Object getValue() {
		return parameter;
	}

	private Object processDialetValue(Object parameter) {
		if (MqlParser.isOracle() && parameter instanceof Boolean) {
			if ((Boolean) parameter) {
				parameter="1";
			} else {
				parameter="0";
			}
		}
		return parameter;
	}
}
