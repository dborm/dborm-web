package com.tbc.paas.mql.parser;

import static com.tbc.paas.mql.util.SqlConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlMetadata;
import com.tbc.paas.mql.domain.SqlNode;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.grammar.SqlGrammar;
import com.tbc.paas.mql.parser.attach.MqlInsertAttach;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlBuilder;

public class MqlInsertParser extends MqlParser {

	private MqlInsertAttach insertAttach;
	private Object mainPkValue;

	public MqlInsertParser() {
		super();
	}

	public MqlInsertParser(SqlGrammar sqlGrammar, List<Object> parameterList,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterList, parameterMap);
	}

	public MqlInsertParser(SqlGrammar sqlGrammar, List<Object> parameterList) {
		super(sqlGrammar, parameterList);
	}

	public MqlInsertParser(SqlGrammar sqlGrammar,
			Map<String, Object> parameterMap) {
		super(sqlGrammar, parameterMap);
	}

	public MqlInsertParser(SqlGrammar sqlGrammar) {
		super(sqlGrammar);
	}

	@Override
	public List<SqlBuilder> parse() {
		mqlAnalyzer.analyze();
		Map<String, SqlBuilder> insertSqlMap = generateInsertTableColumnPart();
		Collection<SqlBuilder> values = insertSqlMap.values();

		return new ArrayList<SqlBuilder>(values);
	}

	private Map<String, SqlBuilder> generateInsertTableColumnPart() {
		Map<String, SqlBuilder> insertColumnMap = new HashMap<String, SqlBuilder>();
		Map<String, SqlBuilder> insertValueMap = new HashMap<String, SqlBuilder>();

		SqlNode rootNode = this.sqlGrammar.getRootNode();
		SqlMetadata sqlMetadata = this.sqlGrammar.getSqlMetadata();
		SqlTable sqlTable = sqlMetadata.getUniqueTable();
		String realTableName = mqlAnalyzer.getRealTableName(sqlTable);
		String sqlTablePk = mqlAnalyzer.getTablePrimaryKey(realTableName);

		mqlNotify.setPrimaryKeyColumn(sqlTablePk);
		mqlNotify.setTableName(realTableName);
		mqlNotify.setMqlOpertation(MqlOperation.INSERT);

		SqlNode columsNode = rootNode.getChild(0);
		SqlNode valuesNode = rootNode.getChild(1);
		int columnCount = columsNode.getChildrenCount();
		for (int i = 0; i < columnCount; i++) {
			SqlNode columnNode = columsNode.getChild(i);
			SqlColumn sqlColumn = (SqlColumn) columnNode.getValue();

			SqlTable belongSqlTbale = mqlAnalyzer
					.getBelongToSqlTable(sqlColumn);
			String belongTableName = belongSqlTbale.getTableName();
			if (!belongSqlTbale.isExtTable()) {
				belongTableName = mqlAnalyzer.getRealTableName(belongSqlTbale);
			}
			String realColumneName = mqlAnalyzer.getRealColumnName(sqlColumn);
			processColumnNode(belongTableName, realColumneName, insertColumnMap);

			SqlNode child = valuesNode.getChild(i);

			Object value = processValueNode(child, belongTableName,
					insertValueMap);

			if (realTableName.equalsIgnoreCase(belongTableName)) {
				if (realColumneName.equals(sqlTablePk)) {
					mainPkValue = value;
					mqlNotify.addAffectedPrimaryKey(mainPkValue.toString());
				} else {
					mqlNotify.putAffectedColumn(realColumneName, value);
				}
			}
		}

		generateInsertValuesPart(insertColumnMap, insertValueMap, realTableName);

		return insertColumnMap;
	}

	private void generateInsertValuesPart(
			Map<String, SqlBuilder> insertColumnMap,
			Map<String, SqlBuilder> insertValueMap, String realTableName) {
		Set<Entry<String, SqlBuilder>> entrySet = insertColumnMap.entrySet();

		for (Entry<String, SqlBuilder> entry : entrySet) {
			String insertTableName = entry.getKey();
			SqlBuilder columnBuilder = entry.getValue();
			SqlBuilder valueBuilder = insertValueMap.get(insertTableName);

			if (insertAttach != null) {
				SqlBuilder columnAttach = insertAttach
						.getColumnAttach(mqlAnalyzer);
				if (!columnAttach.isEmpty()) {
					columnBuilder.append(columnAttach).append(COMMA);
				}

				SqlBuilder valueAttach = insertAttach
						.getValueAttach(mqlAnalyzer);
				if (!valueAttach.isEmpty()) {
					valueBuilder.append(valueAttach).append(COMMA);
				}
			}

			if (!insertTableName.equals(realTableName)) {
				String primaryKey = mqlAnalyzer
						.getTablePrimaryKey(insertTableName);
				columnBuilder.append(primaryKey).append(RIGHT_BRACKET);
				valueBuilder.append(QUESTION).append(RIGHT_BRACKET);
				valueBuilder.addParameter(mainPkValue);
			} else {
				columnBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
				valueBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
			}

			columnBuilder.append(VALUES).append(valueBuilder);
		}
	}

	private void processColumnNode(String realTableName,
			String realColumneName, Map<String, SqlBuilder> insertSqlMap) {
		SqlBuilder columnPartBuilder = insertSqlMap.get(realTableName);
		if (columnPartBuilder == null) {
			columnPartBuilder = new SqlBuilder(INSERT);
			columnPartBuilder.append(INTO).append(realTableName)
					.append(LEFT_BRACKET);
			insertSqlMap.put(realTableName, columnPartBuilder);
		}
		columnPartBuilder.append(realColumneName).append(COMMA);
	}

	private Object processValueNode(SqlNode valueNode, String realTableName,
			Map<String, SqlBuilder> insertSqlValuePart) {

		SqlBuilder valuePartBuilder = insertSqlValuePart.get(realTableName);
		if (valuePartBuilder == null) {
			valuePartBuilder = new SqlBuilder(LEFT_BRACKET);
			insertSqlValuePart.put(realTableName, valuePartBuilder);
		}

		MqlValueParser valueNodeParser = new MqlValueParser(valuePartBuilder,
				parameterList, parameterMap);
		valueNodeParser.processValuePositionNode(valueNode);
		valuePartBuilder.append(COMMA);

		return valueNodeParser.getValue();
	}

	public MqlInsertAttach getInsertAttach() {
		return insertAttach;
	}

	public void setInsertAttach(MqlInsertAttach insertAttach) {
		this.insertAttach = insertAttach;
	}
}
