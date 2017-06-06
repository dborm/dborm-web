package com.tbc.paas.mql.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.util.UUIDGenerator;

public class SqlTableRelCopy implements SqlConstants {
	public static final String OPT_TIME = "opt_time";
	public static final String CORP_CODE = "corp_code";
	public static final String CREATE_BY = "create_by";
	public static final String CREATE_TIME = "create_time";
	public static final String LAST_MODIFY_BY = "last_modify_by";
	public static final String LAST_MODIFY_TIME = "last_modify_time";
	public static final String[] SQL_TAB_NORMAL_COLUMNS = { CORP_CODE,
			CREATE_BY, CREATE_TIME, LAST_MODIFY_BY, LAST_MODIFY_TIME, OPT_TIME };

	protected String fromCorpCode;
	protected String toCorpCode;
	protected Configure configure;
	protected Connection fromConn;
	protected Connection toConn;

	protected SqlAliasGenerator aliasGenerator;
	protected Map<String, Map<String, String>> idMaps;
	protected Map<String, Map<String, String>> preIdMaps;

	public SqlTableRelCopy() {
		super();
		aliasGenerator = new SqlAliasGenerator();
	}

	public List<String> getTabNormalColumns() {
		return Arrays.asList(SQL_TAB_NORMAL_COLUMNS);
	}

	protected SqlBuilder generateUpdateForNormalColumn(String tableName) {
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		String time = currentTime.toString();
		String userId = ExecutionContext.getUserId();

		SqlBuilder bulider = new SqlBuilder(UPDATE);
		bulider.append(tableName, SET, CORP_CODE, EQUAL)
				.appendParameter(toCorpCode).append(COMMA, CREATE_TIME, EQUAL)
				.appendParameter(time).append(COMMA, CREATE_BY, EQUAL)
				.appendParameter(userId).append(COMMA, LAST_MODIFY_TIME, EQUAL)
				.appendParameter(time).append(COMMA, LAST_MODIFY_BY, EQUAL)
				.appendParameter(userId).append(WHERE, CORP_CODE, IS, NULL);

		return bulider;
	}

	protected SqlBuilder generateInsertSql(String tableName,
			List<String> columnList) {
		SqlBuilder columnBuilder = new SqlBuilder(INSERT);
		columnBuilder.append(INTO, tableName, LEFT_BRACKET);

		SqlBuilder valueBuilder = new SqlBuilder(LEFT_BRACKET);
		for (String columnName : columnList) {
			columnBuilder.append(columnName, COMMA);
			valueBuilder.append(QUESTION, COMMA);
		}

		columnBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA)
				.append(VALUES);
		valueBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
		columnBuilder.append(valueBuilder);

		return columnBuilder;
	}

	protected void processSqlDupRel(ResultSet results,
			PreparedStatement ps, List<SqlDupRel> dulRelList,
			int startIndex) throws SQLException {

		int dupSize = dulRelList.size();
		for (int i = 0; i < dupSize; i++) {
			SqlDupRel sqlDupRel = dulRelList.get(i);
			String dupRelShip = sqlDupRel.dupRelShip;

			if (dupRelShip.equalsIgnoreCase(SqlDupRel.DUP_ID)) {
				String dupId = results.getString(i + startIndex);
				if (dupId == null) {
					ps.setNull(i + startIndex, Types.NULL);
					continue;
				}

				Map<String, String> map = idMaps.get(sqlDupRel.dupTabName);

				String dupNewId = map.get(dupId);
				if (dupNewId == null) {
					dupNewId = UUIDGenerator.getUUID();
					Map<String, String> dupPreIdMap = preIdMaps
							.get(sqlDupRel.dupTabName);
					if (dupPreIdMap == null) {
						dupPreIdMap = new HashMap<String, String>();
						preIdMaps.put(sqlDupRel.dupTabName, dupPreIdMap);
					}
					dupPreIdMap.put(dupId, dupNewId);
				}

				ps.setString(i + startIndex, dupNewId);
			} else {
				throw new UnsupportedOperationException("Dup rel(" + sqlDupRel
						+ ") doesn't supported for new!");
			}
		}
	}
	
	protected String getToTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.toTable;
		String alias = aliasGenerator.generate(tableName);
		currentTableAlias.put(tableName, alias);

		return alias;
	}

	protected String getFromTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.fromTable;
		return currentTableAlias.get(tableName);
	}

	protected String getRelTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.relTable;
		String alias = aliasGenerator.generate(tableName);
		currentTableAlias.put(tableName, alias);

		return alias;
	}

	protected void setData(PreparedStatement ps, int index, Object data)
			throws SQLException {
		if (data == null) {
			ps.setNull(index, Types.NULL);
		} else {
			ps.setObject(index, data);
		}
	}

	public Map<String, Map<String, String>> getIdMaps() {
		return idMaps;
	}

	public void setIdMaps(Map<String, Map<String, String>> idMaps) {
		this.idMaps = idMaps;
	}

	public Map<String, Map<String, String>> getPreIdMaps() {
		return preIdMaps;
	}

	public void setPreIdMaps(Map<String, Map<String, String>> preIdMaps) {
		this.preIdMaps = preIdMaps;
	}
}
