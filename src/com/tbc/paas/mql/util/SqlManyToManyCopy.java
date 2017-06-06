package com.tbc.paas.mql.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.util.UUIDGenerator;
import com.tbc.paas.mql.metadata.domain.Column;

public class SqlManyToManyCopy extends SqlTableRelCopy {

	public void copyManyToMany(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			Map<String, String> tableAlias) throws SQLException {
		List<String> toColumnList = getToTabSortedColumnList(sqlTabRel);
		List<String> relColumnList = getRelTabSortedColumnList(sqlTabRel);

		SqlBuilder select = generateManyToManyQuery(fromSqlBuilder,
				conditonBuilder, sqlTabRel, toColumnList, relColumnList,
				tableAlias);
		Statement selector = fromConn.createStatement();

		SqlBuilder insert = generateInsertSql(sqlTabRel.toTable, toColumnList);
		SqlBuilder insertRel = generateRelInsert(sqlTabRel, relColumnList);
		PreparedStatement insertor = toConn.prepareStatement(insert.getSql());
		PreparedStatement relInsertor = toConn.prepareStatement(insertRel
				.getSql());

		String fromTableName = sqlTabRel.fromTable;
		Map<String, String> fromTableIdMap = idMaps.get(fromTableName);
		Map<String, String> toTabIdMap = idMaps.get(sqlTabRel.toTable);
		if (toTabIdMap == null) {
			toTabIdMap = new HashMap<String, String>();
			idMaps.put(sqlTabRel.toTable, toTabIdMap);
		}
		Map<String, String> preIdMap = preIdMaps.get(sqlTabRel.toTable);

		int toColumnSize = toColumnList.size();
		int totalColumnSize = relColumnList.size() + toColumnSize;
		// 这个算法是取(id+dup_id_column+1);
		List<SqlDupRel> dulRelList = sqlTabRel.getDulRelList();
		int startIndex = 1 + dulRelList.size() + 1;
		int relStartIndex = 1 + toColumnSize;

		ResultSet results = selector.executeQuery(select.getSql());
		while (results.next()) {
			String oldId = results.getString(1);
			if (oldId == null) {
				continue;
			}

			if (toTabIdMap.get(oldId) != null) {
				continue;
			}

			String newId = null;
			if (preIdMap != null) {
				newId = preIdMap.get(oldId);
			}

			if (newId == null) {
				newId = UUIDGenerator.getUUID();
			}
			toTabIdMap.put(oldId, newId);
			insertor.setString(1, newId);

			processSqlDupRel(results, insertor, dulRelList, 2);

			for (int i = startIndex; i <= toColumnSize; i++) {
				Object data = results.getObject(i);
				setData(insertor, i, data);
			}
			insertor.addBatch();

			relInsertor.setObject(1, UUIDGenerator.getUUID());
			String relFromId = results.getString(toColumnSize + 1);
			relFromId = fromTableIdMap.get(relFromId);
			relInsertor.setObject(2, newId);
			int index = 3;
			for (int i = relStartIndex; i <= totalColumnSize; i++) {
				Object data = results.getObject(i);
				setData(relInsertor, index, data);
				index++;
			}

			relInsertor.addBatch();
		}

		insertor.executeBatch();
		insertor.close();
		relInsertor.executeBatch();
		relInsertor.close();

		SqlBuilder update = generateUpdateForNormalColumn(sqlTabRel.toTable);
		Statement updateor = toConn.createStatement();
		updateor.executeUpdate(update.getSql());
		String relTableName = sqlTabRel.relTable;
		SqlBuilder relUpdate = generateUpdateForNormalColumn(relTableName);
		updateor.executeUpdate(relUpdate.getSql());
		updateor.close();
	}

	private SqlBuilder generateRelInsert(SqlTabRel sqlTabRel,
			List<String> columnList) {
		String relTableName = sqlTabRel.relTable;
		String relToColumnName = sqlTabRel.relToColumn;

		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(relTableName);
		String primaryColumnName = entityMaping.getPrimaryColumnName();

		List<String> realColumnList = new ArrayList<String>();
		realColumnList.add(primaryColumnName);
		realColumnList.add(relToColumnName);
		realColumnList.addAll(columnList);

		return generateInsertSql(relTableName, realColumnList);
	}

	private List<String> getRelTabSortedColumnList(SqlTabRel sqlTabRel) {
		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(sqlTabRel.relTable);
		String relPrimaryColumn = entityMaping.getPrimaryColumnName();
		String relFromColumnName = sqlTabRel.relFromColumn;
		String relToColumnName = sqlTabRel.relToColumn;

		List<String> removeColumns = getTabNormalColumns();
		removeColumns.add(relPrimaryColumn);
		removeColumns.add(relToColumnName);
		removeColumns.add(relFromColumnName);

		List<String> sortedColumnList = new ArrayList<String>();
		sortedColumnList.add(relFromColumnName);

		List<Column> columnList = entityMaping.getColumnList();
		Iterator<Column> iterator = columnList.iterator();
		while (iterator.hasNext()) {
			Column next = iterator.next();
			String columnName = next.getColumnName();
			if (removeColumns.contains(columnName)) {
				iterator.remove();
			} else {
				sortedColumnList.add(columnName);
			}
		}

		return sortedColumnList;
	}

	private List<String> getToTabSortedColumnList(SqlTabRel sqlTabRel) {
		String tableName = sqlTabRel.toTable;
		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(tableName);
		String toColumnName = sqlTabRel.toColumn;
		String primaryColumn = entityMaping.getPrimaryColumnName();

		List<String> sortedColumnList = new ArrayList<String>();
		sortedColumnList.add(primaryColumn);
		if (!primaryColumn.equalsIgnoreCase(toColumnName)) {
			sortedColumnList.add(toColumnName);
		}

		List<String> filterColumns = getTabNormalColumns();
		filterColumns.add(primaryColumn);
		filterColumns.add(toColumnName);

		List<SqlDupRel> dulRelList = sqlTabRel.getDulRelList();
		for (SqlDupRel sqlDupRel : dulRelList) {
			filterColumns.add(sqlDupRel.columnName);
			sortedColumnList.add(sqlDupRel.columnName);
		}

		List<Column> columnList = entityMaping.getColumnList();
		Iterator<Column> iterator = columnList.iterator();
		while (iterator.hasNext()) {
			Column next = iterator.next();
			String columnName = next.getColumnName();
			if (filterColumns.contains(columnName)) {
				iterator.remove();
			} else {
				sortedColumnList.add(columnName);
			}
		}

		return sortedColumnList;
	}

	private SqlBuilder generateManyToManyQuery(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			List<String> columnList, List<String> relTabColumnList,
			Map<String, String> currentTableAlias) {

		String fromTableAlias = getFromTableAlias(sqlTabRel, currentTableAlias);
		String toTableAlias = getToTableAlias(sqlTabRel, currentTableAlias);

		SqlBuilder selector = new SqlBuilder(SELECT);
		for (String columnName : columnList) {
			selector.appendAndEscapeRest(toTableAlias, DOT, columnName, COMMA);
		}

		String relTableName = sqlTabRel.relTable;
		String relTableAlias = getRelTableAlias(sqlTabRel, currentTableAlias);
		for (String relColumnName : relTabColumnList) {
			selector.appendAndEscapeRest(relTableAlias, DOT, relColumnName,
					COMMA);
		}
		selector.removeLastSlice(COMMA);

		fromSqlBuilder
				.append(LEFT_JOIN, relTableName, relTableAlias, ON)
				.appendAndEscapeRest(relTableAlias, DOT,
						sqlTabRel.relFromColumn).append(EQUAL)
				.appendAndEscapeRest(fromTableAlias, DOT, sqlTabRel.fromColumn)
				.append(LEFT_JOIN, sqlTabRel.toTable, toTableAlias, ON)
				.appendAndEscapeRest(toTableAlias, DOT, sqlTabRel.toColumn)
				.append(EQUAL)
				.appendAndEscapeRest(relTableAlias, DOT, sqlTabRel.relToColumn);

		selector.append(fromSqlBuilder).append(conditonBuilder).append(AND)
				.append(toTableAlias).escapeAppend(DOT).append(CORP_CODE)
				.append(EQUAL).appendParameter(fromCorpCode);

		return selector;
	}

}
