package com.tbc.paas.mql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.ds.MdlDataSourceService;
import com.tbc.paas.mdl.util.UUIDGenerator;
import com.tbc.paas.mql.metadata.domain.Column;

public class SqlDataCopyUtil implements SqlConstants {
	public static final Logger LOG = Logger.getLogger(SqlDataCopyUtil.class
			.getName());

	public static final String CREATE_TIME = "create_time";
	public static final String CREATE_BY = "create_by";
	public static final String LAST_MODIFY_TIME = "last_modify_time";
	public static final String LAST_MODIFY_BY = "last_modify_by";
	public static final String OPT_TIME = "opt_time";
	public static final String CORP_CODE = "corp_code";

	private String fromAppCode;
	private String toAppCode;
	private String toCorpCode;
	private String fromCorpCode;
	private Configure configure;

	private Connection toConn = null;
	private Connection fromConn = null;

	Map<String, Map<String, String>> idMaps;
	Map<String, Map<String, String>> preIdMaps;

	private MdlDataSourceService dataSourceService;
	private SqlAliasGenerator aliasGenerator = new SqlAliasGenerator();

	public static String pgClassName = "org.postgresql.Driver";
	public static String pgUserName = "postgres";
	public static String pgPassword = "postgres";
	public static String pgUrl = "jdbc:postgresql://192.168.0.217:5432/lcms";

	public SqlDataCopyUtil(String fromAppCode, String toAppCode,
			String fromCorpCode, String toCorpCode) {
		super();
		this.fromAppCode = fromAppCode;
		this.toAppCode = toAppCode;
		this.toCorpCode = toCorpCode;
		this.fromCorpCode = fromCorpCode;
		idMaps = new HashMap<String, Map<String, String>>();
		preIdMaps = new HashMap<String, Map<String, String>>();
	}

	public void copy(Class<?> entityClass, List<String> entityIds,
			List<SqlTabRel> tabRelList) {
		try {
			// DataSource toDataSource = dataSourceService.getDataSource(
			// toAppCode, toCorpCode);
			// toConn = toDataSource.getConnection();

			DataSource fromDataSource = dataSourceService.getDataSource(
					fromAppCode, fromCorpCode);

			toConn = getPostgresqlConnection(pgClassName, pgUserName,
					pgPassword, pgUrl);
			toConn.setAutoCommit(false);
			fromConn = fromDataSource.getConnection();

			copyData(entityClass, entityIds, tabRelList);

			toConn.commit();
		} catch (Exception e) {
			try {
				toConn.rollback();
			} catch (SQLException e1) {
				LOG.log(Level.SEVERE, e.getMessage(), e1);
			}
			LOG.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (fromConn != null) {
				try {
					fromConn.close();
				} catch (Exception e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}

			if (toConn != null) {
				try {
					toConn.close();
				} catch (Exception e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	private void copyData(Class<?> entityClass, List<String> entityIds,
			List<SqlTabRel> tabRelList) throws SQLException {
		EntityMaping entityMapping = configure
				.getEntityMappingByClassName(entityClass.getName());
		String tableName = entityMapping.getTableName();
		SqlTabRel sqlTabRel = new SqlTabRel();
		sqlTabRel.toTable = tableName;
		sqlTabRel.toColumn = entityMapping.getPrimaryColumnName();

		SqlBuilder fromBuilder = new SqlBuilder(FROM);
		String primaryColumnName = entityMapping.getPrimaryColumnName();
		SqlBuilder queryCondition = new SqlBuilder();
		queryCondition.escapeAppend(primaryColumnName).append(IN, LEFT_BRACKET)
				.appendParameters(entityIds).append(RIGHT_BRACKET);

		Map<String, String> aliseMap = new HashMap<String, String>();
		copyNormalRelTab(fromBuilder, queryCondition, sqlTabRel, aliseMap);
		ArrayList<SqlTabRel> parentPath = new ArrayList<SqlTabRel>();
		processTabAndRelTab(tableName, fromBuilder, queryCondition, tabRelList,
				parentPath, aliseMap);
	}

	public static Connection getPostgresqlConnection(String className,
			String userName, String password, String url)
			throws ClassNotFoundException, SQLException {
		Class.forName(className);
		return DriverManager.getConnection(url, userName, password);
	}

	private void processTabAndRelTab(String tableName,
			SqlBuilder fromSqlBuilder, SqlBuilder conditonBuilder,
			List<SqlTabRel> tabRelList, List<SqlTabRel> parentPath,
			Map<String, String> currentTableAlias) throws SQLException {

		Iterator<SqlTabRel> iterator = tabRelList.iterator();
		while (iterator.hasNext()) {
			SqlTabRel sqlTabRel = iterator.next();
			if (!sqlTabRel.fromTable.equalsIgnoreCase(tableName)) {
				continue;
			}

			if (parentPath.contains(sqlTabRel)) {
				continue;
			}

			SqlBuilder fb = new SqlBuilder(fromSqlBuilder.getSql());
			SqlBuilder cb = new SqlBuilder(conditonBuilder.getSql());
			Map<String, String> aliseMap = new HashMap<String, String>(
					currentTableAlias);

			if (!SqlTabRel.MANY_TO_MANY.equalsIgnoreCase(sqlTabRel.relation)) {
				copyNormalRelTab(fb, cb, sqlTabRel, aliseMap);
			} else {
				copyManyToManyRelTab(fb, cb, sqlTabRel, aliseMap);
			}

			List<SqlTabRel> childPath = new ArrayList<SqlTabRel>(parentPath);
			childPath.add(sqlTabRel);
			processTabAndRelTab(sqlTabRel.toTable, fb, cb, tabRelList, childPath,
					aliseMap);
		}
	}

	private void copyNormalRelTab(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) throws SQLException {
		EntityMaping fromEntityMaping = configure
				.getEntityMapingByTableName(sqlTabRel.toTable);
		String fromPrimaryColumn = fromEntityMaping.getPrimaryColumnName();

		List<String> columnList = getSortedColumnList(sqlTabRel);
		SqlBuilder select = generateQuery(fromSqlBuilder, conditonBuilder,
				sqlTabRel, columnList, currentTableAlias);
		Statement selector = fromConn.createStatement();
		ResultSet results = selector.executeQuery(select.getSql());

		Map<String, String> tabIdMap = idMaps.get(sqlTabRel.toTable);
		if (tabIdMap == null) {
			tabIdMap = new HashMap<String, String>();
			idMaps.put(sqlTabRel.toTable, tabIdMap);
		}
		Map<String, String> preIdMap = preIdMaps.get(sqlTabRel.toTable);

		SqlBuilder insert = generateInsert(sqlTabRel.toTable, columnList);
		PreparedStatement insertor = toConn.prepareStatement(insert.getSql());
		int totalColumnSize = columnList.size();
		List<SqlDupRel> dulRelList = sqlTabRel.getDulRelList();
		int dupSize = dulRelList.size();
		if ((SqlTabRel.ONE_TO_ONE.equalsIgnoreCase(sqlTabRel.relation) && fromPrimaryColumn
				.equalsIgnoreCase(sqlTabRel.toColumn))
				|| SqlTabRel.MANY_TO_ONE.equalsIgnoreCase(sqlTabRel.relation)) {
			PreparedStatement slaveUpdateor = null;
			if (sqlTabRel.fromTable != null) {
				SqlBuilder slaveUpdate = generateUpdateForSlaveColumn(sqlTabRel);
				slaveUpdateor = toConn.prepareStatement(slaveUpdate.getSql());
			}

			int startIndex = 1 + dupSize + 1;
			while (results.next()) {
				String oldId = results.getString(1);
				if (oldId == null) {
					continue;
				}

				String newId = tabIdMap.get(oldId);
				if (newId != null) {
					continue;
				}

				if (preIdMap != null) {
					newId = preIdMap.get(oldId);
				}

				if (newId == null) {
					newId = UUIDGenerator.getUUID();
				}

				if (slaveUpdateor != null) {
					addDataForPrepareStatement(slaveUpdateor, 1, newId);
					addDataForPrepareStatement(slaveUpdateor, 2, oldId);
					slaveUpdateor.addBatch();
				}

				tabIdMap.put(oldId, newId);
				insertor.setString(1, newId);

				processSqlDupRel(results, insertor, dulRelList, 2);

				for (int i = startIndex; i <= totalColumnSize; i++) {
					Object data = results.getObject(i);
					addDataForPrepareStatement(insertor, i, data);
				}

				insertor.addBatch();
			}

			insertor.executeBatch();
			insertor.close();

			if (slaveUpdateor != null) {
				slaveUpdateor.executeBatch();
				slaveUpdateor.close();
			}

			SqlBuilder update = generateUpdateForNormalColumn(sqlTabRel.toTable);
			Statement updateor = toConn.createStatement();
			updateor.executeUpdate(update.getSql());
			updateor.close();
		} else {
			Map<String, String> fromIdMap = idMaps.get(sqlTabRel.fromTable);
			// 这个算法是取(id+dup_id_column+1);

			int startIndex = 2 + dupSize + 1;
			while (results.next()) {
				String oldId = results.getString(1);
				if (oldId == null) {
					continue;
				}

				String newId = tabIdMap.get(oldId);
				if (newId != null) {
					continue;
				}

				if (preIdMap != null) {
					newId = preIdMap.get(oldId);
				}

				if (newId == null) {
					newId = UUIDGenerator.getUUID();
				}
				tabIdMap.put(oldId, newId);
				insertor.setString(1, newId);

				Object relId = results.getString(2);
				if (relId == null) {
					insertor.setNull(2, Types.NULL);
				} else {
					String newRelId = fromIdMap.get(relId);
					insertor.setObject(2, newRelId);
				}

				processSqlDupRel(results, insertor, dulRelList, 3);

				for (int i = startIndex; i <= totalColumnSize; i++) {
					Object data = results.getObject(i);
					addDataForPrepareStatement(insertor, i, data);
				}

				insertor.addBatch();
			}

			insertor.executeBatch();
			SqlBuilder update = generateUpdateForNormalColumn(sqlTabRel.toTable);
			Statement updateor = toConn.createStatement();
			updateor.executeUpdate(update.getSql());
		}
	}

	private void processSqlDupRel(ResultSet results,
			PreparedStatement insertor, List<SqlDupRel> dulRelList,
			int startIndex) throws SQLException {

		int dupSize = dulRelList.size();
		for (int i = 0; i < dupSize; i++) {
			SqlDupRel sqlDupRel = dulRelList.get(i);
			String dupRelShip = sqlDupRel.dupRelShip;

			if (dupRelShip.equalsIgnoreCase(SqlDupRel.DUP_ID)) {
				String dupId = results.getString(i + startIndex);
				if (dupId == null) {
					insertor.setNull(i + startIndex, Types.NULL);
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

				insertor.setString(i + startIndex, dupNewId);
			} else {
				throw new UnsupportedOperationException("Dup rel(" + sqlDupRel
						+ ") doesn't supported for new!");
			}
		}
	}

	private void copyManyToManyRelTab(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) throws SQLException {

		List<String> toColumnList = getSortedColumnList(sqlTabRel);
		List<String> relColumnList = getRelTabSortedColumnList(sqlTabRel);

		SqlBuilder select = generateManyToManyQuery(fromSqlBuilder,
				conditonBuilder, sqlTabRel, toColumnList, relColumnList,
				currentTableAlias);
		Statement selector = fromConn.createStatement();
		ResultSet results = selector.executeQuery(select.getSql());

		String toTableName = sqlTabRel.toTable;
		SqlBuilder insert = generateInsert(toTableName, toColumnList);
		SqlBuilder insertRel = generateRelInsert(sqlTabRel, relColumnList);
		PreparedStatement insertor = toConn.prepareStatement(insert.getSql());
		PreparedStatement relInsertor = toConn.prepareStatement(insertRel
				.getSql());

		String fromTableName = sqlTabRel.fromTable;
		Map<String, String> fromTableIdMap = idMaps.get(fromTableName);
		Map<String, String> toTabIdMap = idMaps.get(toTableName);
		if (toTabIdMap == null) {
			toTabIdMap = new HashMap<String, String>();
			idMaps.put(toTableName, toTabIdMap);
		}
		Map<String, String> preIdMap = preIdMaps.get(toTableName);

		int toColumnSize = toColumnList.size();
		int totalColumnSize = relColumnList.size() + toColumnSize;
		// 这个算法是取(id+dup_id_column+1);
		List<SqlDupRel> dulRelList = sqlTabRel.getDulRelList();
		int startIndex = 1 + dulRelList.size() + 1;
		int relStartIndex = 1 + toColumnSize;
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
				addDataForPrepareStatement(insertor, i, data);
			}
			insertor.addBatch();

			relInsertor.setObject(1, UUIDGenerator.getUUID());
			String relFromId = results.getString(toColumnSize + 1);
			relFromId = fromTableIdMap.get(relFromId);
			relInsertor.setObject(2, newId);
			int index = 3;
			for (int i = relStartIndex; i <= totalColumnSize; i++) {
				Object data = results.getObject(i);
				addDataForPrepareStatement(relInsertor, index, data);
				index++;
			}

			relInsertor.addBatch();
		}

		insertor.executeBatch();
		insertor.close();
		relInsertor.executeBatch();
		relInsertor.close();

		SqlBuilder update = generateUpdateForNormalColumn(toTableName);
		Statement updateor = toConn.createStatement();
		updateor.executeUpdate(update.getSql());
		String relTableName = sqlTabRel.relTable;
		SqlBuilder relUpdate = generateUpdateForNormalColumn(relTableName);
		updateor.executeUpdate(relUpdate.getSql());
		updateor.close();
	}

	private void addDataForPrepareStatement(PreparedStatement ps, int index,
			Object data) throws SQLException {
		if (data == null) {
			ps.setNull(index, Types.NULL);
		} else {
			ps.setObject(index, data);
		}
	}

	private List<String> getRelTabSortedColumnList(SqlTabRel sqlTabRel) {
		String tableName = sqlTabRel.relTable;
		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(tableName);
		String primaryColumnName = entityMaping.getPrimaryColumnName();
		String relToColumnName = sqlTabRel.relToColumn;
		String relFromColumnName = sqlTabRel.relFromColumn;

		List<Column> columnList = entityMaping.getColumnList();
		Iterator<Column> iterator = columnList.iterator();
		while (iterator.hasNext()) {
			Column next = iterator.next();
			String columnName = next.getColumnName();
			if (columnName.equalsIgnoreCase(CREATE_TIME)
					|| columnName.equalsIgnoreCase(CREATE_BY)
					|| columnName.equalsIgnoreCase(LAST_MODIFY_TIME)
					|| columnName.equalsIgnoreCase(LAST_MODIFY_BY)
					|| columnName.equalsIgnoreCase(OPT_TIME)
					|| columnName.equalsIgnoreCase(CORP_CODE)
					|| columnName.equalsIgnoreCase(primaryColumnName)
					|| columnName.equalsIgnoreCase(relToColumnName)
					|| columnName.equalsIgnoreCase(relFromColumnName)) {
				iterator.remove();
			}
		}

		List<String> sortedColumnList = new ArrayList<String>();
		sortedColumnList.add(relFromColumnName);
		for (Column column : columnList) {
			sortedColumnList.add(column.getColumnName());
		}

		return sortedColumnList;
	}

	private List<String> getSortedColumnList(SqlTabRel sqlTabRel) {
		String tableName = sqlTabRel.toTable;
		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(tableName);
		String toColumnName = sqlTabRel.toColumn;
		String primaryColumnName = entityMaping.getPrimaryColumnName();

		List<String> sortedColumnList = new ArrayList<String>();
		sortedColumnList.add(primaryColumnName);
		if (!primaryColumnName.equalsIgnoreCase(toColumnName)) {
			sortedColumnList.add(toColumnName);
		}

		List<String> filterColumn = new ArrayList<String>();
		filterColumn.add(CREATE_TIME);
		filterColumn.add(CREATE_BY);
		filterColumn.add(LAST_MODIFY_TIME);
		filterColumn.add(LAST_MODIFY_BY);
		filterColumn.add(OPT_TIME);
		filterColumn.add(CORP_CODE);
		filterColumn.add(primaryColumnName);
		filterColumn.add(toColumnName);

		List<SqlDupRel> dulRelList = sqlTabRel.getDulRelList();
		for (SqlDupRel sqlDupRel : dulRelList) {
			filterColumn.add(sqlDupRel.columnName);
			sortedColumnList.add(sqlDupRel.columnName);
		}

		List<Column> columnList = entityMaping.getColumnList();
		Iterator<Column> iterator = columnList.iterator();
		while (iterator.hasNext()) {
			Column next = iterator.next();
			String columnName = next.getColumnName();
			if (filterColumn.contains(columnName)) {
				iterator.remove();
			} else {
				sortedColumnList.add(columnName);
			}
		}

		return sortedColumnList;
	}

	private SqlBuilder generateRelInsert(SqlTabRel sqlTabRel,
			List<String> columnList) {
		String relTableName = sqlTabRel.relTable;
		String relToColumnName = sqlTabRel.relToColumn;

		EntityMaping entityMaping = configure
				.getEntityMapingByTableName(relTableName);
		String primaryColumnName = entityMaping.getPrimaryColumnName();

		List<String> realColumnList = new ArrayList<String>(
				columnList.size() + 2);
		realColumnList.add(primaryColumnName);
		realColumnList.add(relToColumnName);
		realColumnList.addAll(columnList);

		return generateInsert(relTableName, realColumnList);
	}

	private SqlBuilder generateQuery(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			List<String> columnList, Map<String, String> currentTableAlias) {

		String toTableName = sqlTabRel.toTable;
		String toTableAlias = getToTableAlias(sqlTabRel, currentTableAlias);

		SqlBuilder selector = new SqlBuilder(SELECT);
		selector.repeatAppend(toTableAlias + DOT, columnList, COMMA)
				.removeLastSlice(COMMA);

		if (sqlTabRel.fromTable != null) {
			String fromTableAlias = getFromTableAlias(sqlTabRel,
					currentTableAlias);
			String fromColumnName = sqlTabRel.fromColumn;
			String toColumnName = sqlTabRel.toColumn;

			fromSqlBuilder.append(LEFT_JOIN).append(toTableName)
					.append(toTableAlias, ON)
					.appendAndEscapeRest(fromTableAlias, DOT, fromColumnName)
					.append(EQUAL)
					.appendAndEscapeRest(toTableAlias, DOT, toColumnName);

		} else {
			fromSqlBuilder.append(toTableName, toTableAlias);
			conditonBuilder.insertStartSlice(WHERE + " " + toTableAlias + DOT);
		}

		selector.append(fromSqlBuilder).append(conditonBuilder);

		if (sqlTabRel.fromTable != null) {
			selector.append(AND)
					.appendAndEscapeRest(toTableAlias, DOT, CORP_CODE)
					.append(EQUAL).appendParameter(fromCorpCode);
		}

		return selector;
	}

	private SqlBuilder generateManyToManyQuery(SqlBuilder fromSqlBuilder,
			SqlBuilder conditonBuilder, SqlTabRel sqlTabRel,
			List<String> columnList, List<String> relTabColumnList,
			Map<String, String> currentTableAlias) {

		String fromTableAlias = getFromTableAlias(sqlTabRel, currentTableAlias);
		String fromColumnName = sqlTabRel.fromColumn;

		String toTableName = sqlTabRel.toTable;
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

		String toColumnName = sqlTabRel.toColumn;
		String relFromColumnName = sqlTabRel.relFromColumn;
		String relToColumnName = sqlTabRel.relToColumn;

		fromSqlBuilder.append(LEFT_JOIN, relTableName, relTableAlias, ON)
				.appendAndEscapeRest(relTableAlias, DOT, relFromColumnName)
				.append(EQUAL)
				.appendAndEscapeRest(fromTableAlias, DOT, fromColumnName)
				.append(LEFT_JOIN, toTableName, toTableAlias, ON)
				.appendAndEscapeRest(toTableAlias, DOT, toColumnName)
				.append(EQUAL)
				.appendAndEscapeRest(relTableAlias, DOT, relToColumnName);

		selector.append(fromSqlBuilder).append(conditonBuilder).append(AND)
				.append(toTableAlias).escapeAppend(DOT).append(CORP_CODE)
				.append(EQUAL).appendParameter(fromCorpCode);

		return selector;
	}

	private SqlBuilder generateInsert(String tableName, List<String> columnList) {
		SqlBuilder insertBuilder = new SqlBuilder(INSERT);
		insertBuilder.append(INTO).append(tableName).append(LEFT_BRACKET);
		SqlBuilder valueBuilder = new SqlBuilder(LEFT_BRACKET);

		for (String columnName : columnList) {
			insertBuilder.append(columnName).append(COMMA);
			valueBuilder.append(SqlConstants.QUESTION).append(COMMA);
		}

		insertBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA)
				.append(VALUES);
		valueBuilder.replaceOrAddLastSlice(RIGHT_BRACKET, COMMA);
		insertBuilder.append(valueBuilder);

		return insertBuilder;
	}

	private SqlBuilder generateUpdateForSlaveColumn(SqlTabRel sqlTabRel) {
		SqlBuilder sqlBuilder = new SqlBuilder(UPDATE);
		sqlBuilder.append(sqlTabRel.fromTable);
		String fromColumnName = sqlTabRel.fromColumn;
		sqlBuilder.append(SET, fromColumnName, EQUAL, QUESTION, WHERE,
				fromColumnName, EQUAL, QUESTION);

		return sqlBuilder;
	}

	private SqlBuilder generateUpdateForNormalColumn(String tableName) {
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		String time = currentTime.toString();
		String userId = ExecutionContext.getUserId();

		SqlBuilder updateNormalColumn = new SqlBuilder(UPDATE);
		updateNormalColumn.append(tableName).append(SET).append(CORP_CODE)
				.append(EQUAL).appendParameter(toCorpCode).append(COMMA)
				.append(CREATE_TIME).append(EQUAL).appendParameter(time)
				.append(COMMA).append(CREATE_BY).append(EQUAL)
				.appendParameter(userId).append(COMMA).append(LAST_MODIFY_TIME)
				.append(EQUAL).appendParameter(time).append(COMMA)
				.append(LAST_MODIFY_BY).append(EQUAL).appendParameter(userId)
				.append(WHERE).append(CORP_CODE).append(IS).append(NULL);

		return updateNormalColumn;
	}

	public String getToTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.toTable;
		String alias = aliasGenerator.generate(tableName);
		currentTableAlias.put(tableName, alias);

		return alias;
	}

	public String getFromTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.fromTable;
		return currentTableAlias.get(tableName);
	}

	public String getRelTableAlias(SqlTabRel sqlTabRel,
			Map<String, String> currentTableAlias) {
		String tableName = sqlTabRel.relTable;
		String alias = aliasGenerator.generate(tableName);
		currentTableAlias.put(tableName, alias);

		return alias;
	}

	public String getFromAppCode() {
		return fromAppCode;
	}

	public void setFromAppCode(String fromAppCode) {
		this.fromAppCode = fromAppCode;
	}

	public String getToAppCode() {
		return toAppCode;
	}

	public void setToAppCode(String toAppCode) {
		this.toAppCode = toAppCode;
	}

	public String getToCorpCode() {
		return toCorpCode;
	}

	public void setToCorpCode(String toCorpCode) {
		this.toCorpCode = toCorpCode;
	}

	public String getFromCorpCode() {
		return fromCorpCode;
	}

	public void setFromCorpCode(String fromCorpCode) {
		this.fromCorpCode = fromCorpCode;
	}

	public MdlDataSourceService getDataSourceService() {
		return dataSourceService;
	}

	public void setDataSourceService(MdlDataSourceService dataSourceService) {
		this.dataSourceService = dataSourceService;
	}

	public Configure getConfigure() {
		return configure;
	}

	public void setConfigure(Configure configure) {
		this.configure = configure;
	}
}
