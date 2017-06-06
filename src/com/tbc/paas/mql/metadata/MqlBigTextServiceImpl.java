package com.tbc.paas.mql.metadata;

import java.util.List;
import java.util.Map;

public class MqlBigTextServiceImpl implements MqlBigTextService {

	@Override
	public String queryBigTextColumn(String appCode, String tableName,
			String rowId, String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> queryBigTextColumnList(String appCode,
			String tableName, List<String> rowIds, String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, String>> queryBigTextColumns(String appCode,
			String tableName, List<String> rowIds, List<String> columnNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveBigTextColumn(String appCode, String tableName,
			String rowId, String columnName, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveBigTextColumn(String appCode, String tableName,
			String rowId, Map<String, String> columnMap) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBigTextColumn(String appCode, String tableName,
			String rowId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBigTextColumn(String appCode, String tableName,
			List<String> rowIds) {
		// TODO Auto-generated method stub

	}

}
