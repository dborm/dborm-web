package com.tbc.paas.mdl.mql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.tbc.framework.util.ExecutionContext;
import com.tbc.paas.mdl.cfg.Configure;
import com.tbc.paas.mdl.cfg.domain.EntityMaping;
import com.tbc.paas.mdl.domain.MdlException;
import com.tbc.paas.mql.analyzer.MqlAnalyzer;
import com.tbc.paas.mql.domain.SqlColumn;
import com.tbc.paas.mql.domain.SqlMetadata;
import com.tbc.paas.mql.domain.SqlPhase;
import com.tbc.paas.mql.domain.SqlTable;
import com.tbc.paas.mql.metadata.MqlMetadataService;
import com.tbc.paas.mql.metadata.domain.Column;
import com.tbc.paas.mql.metadata.domain.CorpColumn;
import com.tbc.paas.mql.metadata.domain.CorpTable;
import com.tbc.paas.mql.metadata.domain.Table;
import com.tbc.paas.mql.metadata.domain.TableView;
import com.tbc.paas.mql.util.MqlOperation;
import com.tbc.paas.mql.util.SqlConstants;

public class MdlAnalyzer extends MqlAnalyzer {

	private Configure configure;
	private SqlMetadata sqlMetadata;
	private MqlOperation mqlOperation;

	private Map<String, String> tablePrimaryKeyMap;
	private Map<SqlTable, List<SqlColumn>> extraSqlTableColumnMap;

	public MdlAnalyzer() {
		super();
		prepareExtraSqlColumn();
	}

	public MdlAnalyzer(String appCode, String corpCode) {
		super(appCode, corpCode);
		prepareExtraSqlColumn();
	}

	public MdlAnalyzer(String appCode, String corpCode, SqlMetadata sqlMetadata) {
		super(appCode, corpCode);
		this.sqlMetadata = sqlMetadata;
		prepareExtraSqlColumn();
	}

	public MdlAnalyzer(String appCode, String corpCode,
			SqlMetadata sqlMetadata, MqlMetadataService metadataService) {
		super(appCode, corpCode, metadataService);
		this.sqlMetadata = sqlMetadata;
		prepareExtraSqlColumn();
	}

	@Override
	public String getRealColumnName(SqlColumn sqlColumn) {
		SqlTable sqlTable = this.sqlColumnTableMap.get(sqlColumn);
		String tableAlias = sqlTable.getTableAlias();

		String columnName = null;
		Column mdlColumn = this.sqlColumnColumnMap.get(sqlColumn);
		if (mdlColumn != null) {
			String tableName = sqlTable.getTableName();
			EntityMaping entityMapping = configure
					.getEntityMappingByClassName(tableName);
			Map<String, String> fieldNameColumnMap = entityMapping
					.getFieldNameColumnNameMap();
			String fieldName = mdlColumn.getColumnName();
			columnName = fieldNameColumnMap.get(fieldName);
		} else {
			CorpColumn mdlCorpColumn = this.sqlColumnCorpColumnMap
					.get(sqlColumn);
			columnName = mdlCorpColumn.getExtColumnName();
		}

		String actualColumnName = null;
		if (mqlOperation == MqlOperation.SELECT) {
			actualColumnName = tableAlias + SqlConstants.DOT + columnName;
		} else {
			actualColumnName = columnName;
		}

		return actualColumnName;
	}

	@Override
	public String getRealTableName(SqlTable sqlTable) {
		String entityName = sqlTable.getTableName();
		return configure.getTableNameByEntityClassName(entityName);
	}

	@Override
	public List<SqlColumn> getSqlColumnList() {
		List<SqlColumn> sqlColumnList = sqlMetadata.getAllColumns();
		if (extraSqlTableColumnMap == null) {
			return sqlColumnList;
		}

		Collection<List<SqlColumn>> extraSqlColumnCollection = extraSqlTableColumnMap
				.values();
		for (List<SqlColumn> extraSqlColumnList : extraSqlColumnCollection) {
			if (extraSqlColumnList == null) {
				continue;
			}

			Iterator<SqlColumn> iterator = extraSqlColumnList.iterator();
			while (iterator.hasNext()) {
				SqlColumn extraSqlColumn = iterator.next();
				if (sqlColumnList.contains(extraSqlColumn)) {
					iterator.remove();
				}
			}

			sqlColumnList.addAll(extraSqlColumnList);
		}

		return sqlColumnList;
	}

	@Override
	public List<SqlTable> getSqlTableList() {
		List<SqlTable> sqlTableList = sqlMetadata.getSqlTableList();
		if (extraSqlTableColumnMap == null) {
			return sqlTableList;
		}

		Set<SqlTable> extraSqlTableSet = extraSqlTableColumnMap.keySet();
		for (SqlTable extraSqlTable : extraSqlTableSet) {
			String extraTableName = extraSqlTable.getTableName();
			for (SqlTable sqlTable : sqlTableList) {
				String tableName = sqlTable.getTableName();
				if (tableName.equals(extraTableName)) {
					continue;
				}
				extraSqlTableColumnMap.remove(extraSqlTable);
			}
		}

		return sqlTableList;
	}

	@Override
	public String getTablePrimaryKey(String realTableName) {
		return tablePrimaryKeyMap.get(realTableName);
	}

	public Configure getConfigure() {
		return configure;
	}

	public void setConfigure(Configure configure) {
		this.configure = configure;
	}

	public SqlMetadata getSqlMetadata() {
		return sqlMetadata;
	}

	public void setSqlMetadata(SqlMetadata sqlMetadata) {
		this.sqlMetadata = sqlMetadata;
	}

	@Override
	public void analyze() {
		super.analyze();
		initTablePrimaryKeyMap();
	}

	private void prepareExtraSqlColumn() {
		sqlTableColumnMap = new HashMap<SqlTable, List<SqlColumn>>();
		Map<String, List<String>> tableColumns = analyzeExtraFields();
		Set<SqlTable> sqlTableSet = sqlTableColumnMap.keySet();
		for (SqlTable sqlTable : sqlTableSet) {
			String realTableName = getRealTableName(sqlTable);
			List<String> extColumnNameList = tableColumns.get(realTableName);
			if (extColumnNameList == null) {
				continue;
			}

			EntityMaping entityMapingByTableName = configure
					.getEntityMapingByTableName(realTableName);
			for (String extraColumnname : extColumnNameList) {
				String fieldName = entityMapingByTableName
						.getFieldNameByColumnName(extraColumnname);
				List<SqlColumn> sqlTableColumnList = sqlTableColumnMap
						.get(sqlTable);
				boolean find = false;
				for (SqlColumn sqlColumn : sqlTableColumnList) {
					String actualColumnName = sqlColumn.getActualColumnName();
					if (actualColumnName.equals(fieldName)) {
						find = true;
						break;
					}
				}

				if (!find) {
					SqlColumn sqlColumn = new SqlColumn(extraColumnname,
							SqlPhase.SELECT);
					List<SqlColumn> extraSqlColumnList = extraSqlTableColumnMap
							.get(sqlTable);
					if (extraSqlColumnList == null) {
						extraSqlColumnList = new ArrayList<SqlColumn>();
						extraSqlTableColumnMap
								.put(sqlTable, extraSqlColumnList);
					}

					extraSqlColumnList.add(sqlColumn);
				}
			}

		}
	}

	private Map<String, List<String>> analyzeExtraFields() {
		Map<String, List<String>> tableColumns = new HashMap<String, List<String>>();
		String extraFields = ExecutionContext.getExtraFields();
		if (extraFields == null || extraFields.trim().isEmpty()) {
			return tableColumns;
		}

		StringTokenizer tokenizer = new StringTokenizer(extraFields, ".,",
				false);
		int countTokens = tokenizer.countTokens();
		if (countTokens % 2 != 0) {
			throw new MdlException(
					"Extra fields are writen with incorrect format!");
		}
		while (tokenizer.hasMoreTokens()) {
			String tableName = tokenizer.nextToken();
			String columnName = tokenizer.nextToken();

			List<String> columnList = tableColumns.get(tableName);
			if (columnList == null) {
				columnList = new ArrayList<String>();
				tableColumns.put(tableName, columnList);
			}
			columnList.add(columnName);
		}

		return tableColumns;
	}

	public Map<String, String> initTablePrimaryKeyMap() {

		tablePrimaryKeyMap = new HashMap<String, String>();

		Collection<TableView> mianTableSet = tableMetadataViewMap.values();
		for (TableView mdlTableView : mianTableSet) {
			Table mainTable = mdlTableView.getMainTable();
			String tableName = mainTable.getTableName();
			EntityMaping entityMapping = configure
					.getEntityMappingByClassName(tableName);

			String pkName = entityMapping.getPrimaryColumnName();
			String realTableName = entityMapping.getTableName();
			tablePrimaryKeyMap.put(realTableName, pkName);

			Map<String, CorpTable> extTables = mdlTableView.getExtTables();
			if (extTables == null) {
				continue;
			}

			Collection<CorpTable> corpTableCollection = extTables.values();
			for (CorpTable corpTable : corpTableCollection) {
				tablePrimaryKeyMap.put(corpTable.getExtTableName(),
						corpTable.getExtPkName());
			}
		}

		return tablePrimaryKeyMap;
	}

	public MqlOperation getOperation() {
		return mqlOperation;
	}

	public void setOperation(MqlOperation operation) {
		this.mqlOperation = operation;
	}

	public Map<SqlTable, List<SqlColumn>> getExtraSqlTableColumnMap() {
		return extraSqlTableColumnMap;
	}

	public void setExtraSqlTableColumnMap(
			Map<SqlTable, List<SqlColumn>> extraSqlTableColumnMap) {
		this.extraSqlTableColumnMap = extraSqlTableColumnMap;
	}
}
